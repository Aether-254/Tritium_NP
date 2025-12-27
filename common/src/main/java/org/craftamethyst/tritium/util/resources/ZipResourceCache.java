package org.craftamethyst.tritium.util.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class ZipResourceCache implements IResourceCache {
    private final FileSystem fs;
    private final Map<String, Path> pathIndex = new ConcurrentHashMap<>();
    private final Map<String, byte[]> memoryCache;
    private final Map<PackType, Set<String>> namespaceCache = new EnumMap<>(PackType.class);
    private final Map<String, List<String>> overlayMap = new ConcurrentHashMap<>();
    private final String[] overlayOrder;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final boolean lazyLoad;

    public ZipResourceCache(Path zip, List<String> overlays, boolean lazy) throws IOException {
        this.fs = FileSystems.newFileSystem(zip);
        this.lazyLoad = lazy;
        this.overlayOrder = buildOverlayOrder(overlays);

        if (lazy) {
            this.memoryCache = Collections.synchronizedMap(new LruCache(8192));
            buildPathIndex();
        } else {
            this.memoryCache = new ConcurrentHashMap<>();
            loadAll();
        }

        for (PackType t : PackType.values()) {
            namespaceCache.put(t, ConcurrentHashMap.newKeySet());
        }
    }

    private String[] buildOverlayOrder(List<String> overlays) {
        if (overlays.isEmpty()) return new String[]{""};
        String[] order = new String[overlays.size() + 1];
        for (int i = 0; i < overlays.size(); i++) {
            order[i] = overlays.get(overlays.size() - 1 - i) + "/";
        }
        order[overlays.size()] = "";
        return order;
    }

    private void buildPathIndex() throws IOException {
        try (Stream<Path> stream = Files.walk(fs.getPath("/"))) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                String normalized = normalize(p);
                pathIndex.put(normalized, p);
                indexNamespace(normalized);
            });
        }
    }

    private void loadAll() throws IOException {
        try (Stream<Path> stream = Files.walk(fs.getPath("/"))) {
            stream.filter(Files::isRegularFile).parallel().forEach(p -> {
                try {
                    String path = normalize(p);
                    byte[] data = Files.readAllBytes(p);
                    memoryCache.put(path, data);
                    indexNamespace(path);
                } catch (IOException ignored) {}
            });
        }
    }

    private void indexNamespace(String path) {
        for (PackType type : PackType.values()) {
            String prefix = type.getDirectory() + "/";
            if (!path.startsWith(prefix)) continue;
            String remaining = path.substring(prefix.length());
            int slash = remaining.indexOf('/');
            if (slash > 0) {
                String ns = remaining.substring(0, slash);
                if (isValidNamespace(ns)) {
                    namespaceCache.get(type).add(ns);
                }
            }
            break;
        }
    }

    @Override
    public byte @Nullable [] getResource(PackType type, ResourceLocation loc) {
        String baseKey = type.getDirectory() + "/" + loc.getNamespace() + "/" + loc.getPath();

        for (String overlay : overlayOrder) {
            String fullKey = overlay + baseKey;
            byte[] data = getData(fullKey);
            if (data != null) return data;
        }
        return null;
    }

    @Override
    public byte @Nullable [] getRootResource(String... parts) {
        String key = String.join("/", parts);
        return getData(key);
    }

    private byte @Nullable [] getData(String key) {
        lock.readLock().lock();
        try {
            byte[] cached = memoryCache.get(key);
            if (cached != null) return cached;

            if (lazyLoad) {
                Path path = pathIndex.get(key);
                if (path != null) {
                    try {
                        byte[] data = Files.readAllBytes(path);
                        memoryCache.put(key, data);
                        return data;
                    } catch (IOException ignored) {}
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Collections.unmodifiableSet(namespaceCache.getOrDefault(type, Set.of()));
    }

    @Override
    public void listResources(PackType type, String ns, String path, ResourceConsumer consumer) {
        String prefix = type.getDirectory() + '/' + ns + '/';
        String dirPrefix = path.isEmpty() ? prefix : prefix + path + '/';

        Set<String> processed = new HashSet<>();
        for (String overlay : overlayOrder) {
            String overlayPrefix = overlay + prefix;
            String overlayDir = overlay + dirPrefix;

            for (Map.Entry<String, byte[]> entry : memoryCache.entrySet()) {
                String filePath = entry.getKey();
                if (!filePath.startsWith(overlayPrefix)) continue;

                if (path.isEmpty()) {
                    int end = filePath.indexOf('/', overlayPrefix.length());
                    if (end > 0) {
                        String rel = filePath.substring(overlayPrefix.length(), end);
                        if (processed.add(rel)) {
                            ResourceLocation loc = ResourceLocation.tryBuild(ns, rel);
                            if (loc != null) consumer.accept(loc, entry.getValue());
                        }
                    }
                } else if (filePath.startsWith(overlayDir)) {
                    String rel = filePath.substring(overlayPrefix.length());
                    if (processed.add(rel)) {
                        ResourceLocation loc = ResourceLocation.tryBuild(ns, rel);
                        if (loc != null) consumer.accept(loc, entry.getValue());
                    }
                }
            }

            if (lazyLoad) {
                for (Map.Entry<String, Path> entry : pathIndex.entrySet()) {
                    String filePath = entry.getKey();
                    if (!filePath.startsWith(overlayPrefix)) continue;

                    if (path.isEmpty()) {
                        int end = filePath.indexOf('/', overlayPrefix.length());
                        if (end > 0) {
                            String rel = filePath.substring(overlayPrefix.length(), end);
                            if (processed.add(rel)) {
                                byte[] data = getData(filePath);
                                if (data != null) {
                                    ResourceLocation loc = ResourceLocation.tryBuild(ns, rel);
                                    if (loc != null) consumer.accept(loc, data);
                                }
                            }
                        }
                    } else if (filePath.startsWith(overlayDir)) {
                        String rel = filePath.substring(overlayPrefix.length());
                        if (processed.add(rel)) {
                            byte[] data = getData(filePath);
                            if (data != null) {
                                ResourceLocation loc = ResourceLocation.tryBuild(ns, rel);
                                if (loc != null) consumer.accept(loc, data);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            memoryCache.clear();
            pathIndex.clear();
            namespaceCache.clear();
            overlayMap.clear();
            if (fs.isOpen()) fs.close();
        } catch (IOException ignored) {
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static String normalize(Path p) {
        String s = p.toString();
        return s.startsWith("/") ? s.substring(1) : s;
    }

    private static boolean isValidNamespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ||
                    c == '_' || c == '-' || c == '.')) {
                return false;
            }
        }
        return !s.isEmpty();
    }

    private static class LruCache extends LinkedHashMap<String, byte[]> {
        private final int maxSize;

        LruCache(int maxSize) {
            super(16, 0.75f, true);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > maxSize;
        }
    }

    public interface OverlayStrategy {
        boolean isOverlayPath(String path);
    }

    public static class DefaultOverlayStrategy implements OverlayStrategy {
        @Override
        public boolean isOverlayPath(String path) {
            int slash = path.indexOf('/');
            return slash > 0 && path.substring(0, slash).indexOf('/') == -1;
        }
    }
}