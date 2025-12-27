package org.craftamethyst.tritium.util.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ZipResourceCache implements IResourceCache {
    private final FileSystem zipFileSystem;
    private final OverlayStrategy overlayStrategy;
    private final String[] overlayPrefixes;

    private final Map<String, Path> lazyPathIndex = new ConcurrentHashMap<>();
    private final Map<String, byte[]> memoryCache;
    private final Map<PackType, Set<String>> namespaceCache = new EnumMap<>(PackType.class);
    private final Map<String, List<CachedResourceEntry>> resourceIndex = new ConcurrentHashMap<>();

    private final boolean useLazyLoading;

    public ZipResourceCache(Path zipPath, List<String> overlays,
                                    OverlayStrategy strategy) throws IOException {
        this(zipPath, overlays, strategy, true, 8192);
    }

    public ZipResourceCache(Path zipPath, List<String> overlays,
                                    OverlayStrategy strategy, boolean lazy,
                                    int cacheSize) throws IOException {
        this.zipFileSystem = FileSystems.newFileSystem(zipPath);
        this.overlayStrategy = strategy;
        this.overlayPrefixes = buildOptimizedOverlayPrefixes(overlays);
        this.useLazyLoading = lazy;

        for (PackType type : PackType.values()) {
            namespaceCache.put(type, ConcurrentHashMap.newKeySet());
        }

        if (useLazyLoading) {
            this.memoryCache = Collections.synchronizedMap(new LruMemoryCache(cacheSize));
            buildPathIndex();
        } else {
            this.memoryCache = new ConcurrentHashMap<>();
            loadAllResources();
        }
    }

    private String[] buildOptimizedOverlayPrefixes(List<String> overlays) {
        if (overlays.isEmpty()) {
            return new String[]{""};
        }

        String[] prefixes = new String[overlays.size() + 1];
        for (int i = 0; i < overlays.size(); i++) {
            String overlay = overlays.get(overlays.size() - 1 - i);
            prefixes[i] = overlay + "/";
        }
        prefixes[overlays.size()] = "";
        return prefixes;
    }

    private void buildPathIndex() throws IOException {
        ResourceIndexer indexer = new ResourceIndexer(namespaceCache, overlayStrategy);

        try (Stream<Path> stream = Files.walk(zipFileSystem.getPath("/"))) {
            stream.filter(Files::isRegularFile)
                    .parallel()
                    .forEach(filePath -> {
                        String relativePath = normalizePath(filePath);
                        lazyPathIndex.put(relativePath, filePath);
                        indexResourcePath(relativePath);
                        indexer.indexPath(relativePath);
                    });
        }
    }

    private void loadAllResources() throws IOException {
        ResourceIndexer indexer = new ResourceIndexer(namespaceCache, overlayStrategy);

        try (Stream<Path> stream = Files.walk(zipFileSystem.getPath("/"))) {
            stream.filter(Files::isRegularFile)
                    .parallel()
                    .forEach(filePath -> {
                        try {
                            String relativePath = normalizePath(filePath);
                            byte[] data = Files.readAllBytes(filePath);
                            memoryCache.put(relativePath, data);
                            indexResource(relativePath, data);
                            indexer.indexPath(relativePath);
                        } catch (IOException ignored) {}
                    });
        }
    }

    private void indexResourcePath(String path) {
        for (PackType type : PackType.values()) {
            String typePrefix = type.getDirectory() + "/";
            if (path.startsWith(typePrefix)) {
                String remainingPath = path.substring(typePrefix.length());
                int namespaceEnd = remainingPath.indexOf('/');
                if (namespaceEnd > 0) {
                    String namespace = remainingPath.substring(0, namespaceEnd);
                    String resourcePath = remainingPath.substring(namespaceEnd + 1);
                    String resourceKey = type.getDirectory() + "/" + namespace + "/" + resourcePath;
                    String overlayKey = determineOverlayKey(path);

                    resourceIndex.computeIfAbsent(resourceKey, k -> new ArrayList<>())
                            .add(new CachedResourceEntry(overlayKey, null));
                }
                break;
            }
        }
    }

    private void indexResource(String path, byte[] data) {
        for (PackType type : PackType.values()) {
            String typePrefix = type.getDirectory() + "/";
            if (path.startsWith(typePrefix)) {
                String remainingPath = path.substring(typePrefix.length());
                int namespaceEnd = remainingPath.indexOf('/');
                if (namespaceEnd > 0) {
                    String namespace = remainingPath.substring(0, namespaceEnd);
                    String resourcePath = remainingPath.substring(namespaceEnd + 1);
                    String resourceKey = type.getDirectory() + "/" + namespace + "/" + resourcePath;
                    String overlayKey = determineOverlayKey(path);

                    resourceIndex.computeIfAbsent(resourceKey, k -> new ArrayList<>())
                            .add(new CachedResourceEntry(overlayKey, data));
                }
                break;
            }
        }
    }

    private String determineOverlayKey(String path) {
        if (!overlayStrategy.isOverlayPath(path)) {
            return "";
        }
        int firstSlash = path.indexOf('/');
        if (firstSlash == -1) {
            return "";
        }
        String overlayName = path.substring(0, firstSlash);
        for (String prefix : overlayPrefixes) {
            if (prefix.startsWith(overlayName + "/")) {
                return overlayName;
            }
        }
        return "";
    }

    @Override
    public byte @Nullable [] getResource(PackType type, ResourceLocation location) {
        String resourceKey = type.getDirectory() + "/" + location.getNamespace() + "/" + location.getPath();
        List<CachedResourceEntry> entries = resourceIndex.get(resourceKey);

        if (entries == null) {
            return null;
        }

        for (String prefix : overlayPrefixes) {
            String overlayKey = prefix.isEmpty() ? "" : prefix.substring(0, prefix.length() - 1);
            for (CachedResourceEntry entry : entries) {
                if (entry.overlayKey.equals(overlayKey)) {
                    return getResourceData(entry, resourceKey);
                }
            }
        }

        return null;
    }

    private byte @Nullable [] getResourceData(CachedResourceEntry entry, String key) {
        if (entry.data != null) {
            return entry.data;
        }

        if (useLazyLoading) {
            Path filePath = lazyPathIndex.get(key);
            if (filePath != null) {
                try {
                    byte[] data = Files.readAllBytes(filePath);
                    entry.data = data;
                    memoryCache.put(key, data);
                    return data;
                } catch (IOException e) {
                    return null;
                }
            }
        }

        return null;
    }

    @Override
    public byte @Nullable [] getRootResource(String... parts) {
        String path = String.join("/", parts);

        byte[] cached = memoryCache.get(path);
        if (cached != null) {
            return cached;
        }

        if (useLazyLoading) {
            Path filePath = lazyPathIndex.get(path);
            if (filePath != null) {
                try {
                    byte[] data = Files.readAllBytes(filePath);
                    memoryCache.put(path, data);
                    return data;
                } catch (IOException e) {
                    return null;
                }
            }
        }

        return null;
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Collections.unmodifiableSet(namespaceCache.getOrDefault(type, Collections.emptySet()));
    }

    @Override
    public void listResources(PackType type, String namespace,
                              String path, ResourceConsumer consumer) {
        String nsPrefix = type.getDirectory() + '/' + namespace + '/';
        String dirPrefix = nsPrefix + (path.isEmpty() ? "" : path + '/');
        Set<String> processed = new HashSet<>();

        for (String prefix : overlayPrefixes) {
            String fullPrefix = prefix + nsPrefix;
            String fullDir = prefix + dirPrefix;

            if (useLazyLoading) {
                for (Map.Entry<String, Path> entry : lazyPathIndex.entrySet()) {
                    String filePath = entry.getKey();
                    if (filePath.startsWith(fullPrefix)) {
                        processResourcePath(filePath, fullPrefix, fullDir, namespace,
                                path.isEmpty(), processed, consumer);
                    }
                }
            } else {
                for (Map.Entry<String, byte[]> entry : memoryCache.entrySet()) {
                    String filePath = entry.getKey();
                    if (filePath.startsWith(fullPrefix)) {
                        processResource(filePath, entry.getValue(), fullPrefix,
                                fullDir, namespace, path.isEmpty(), processed, consumer);
                    }
                }
            }
        }
    }

    private void processResourcePath(String filePath, String fullPrefix, String fullDir,
                                     String namespace, boolean isDirectory,
                                     Set<String> processed, ResourceConsumer consumer) {
        if (isDirectory) {
            int nextSlash = filePath.indexOf('/', fullPrefix.length());
            if (nextSlash != -1 && nextSlash < filePath.length() - 1) {
                String relPath = filePath.substring(fullPrefix.length(), nextSlash + 1);
                if (!processed.contains(relPath)) {
                    processed.add(relPath);
                    ResourceLocation loc = ResourceLocation.tryBuild(namespace, relPath);
                    if (loc != null) {
                        byte[] data = getRootResource(filePath.split("/"));
                        if (data != null) {
                            consumer.accept(loc, data);
                        }
                    }
                }
            }
        } else if (filePath.startsWith(fullDir)) {
            String relPath = filePath.substring(fullPrefix.length());
            if (!processed.contains(relPath)) {
                processed.add(relPath);
                ResourceLocation loc = ResourceLocation.tryBuild(namespace, relPath);
                if (loc != null) {
                    byte[] data = getRootResource(filePath.split("/"));
                    if (data != null) {
                        consumer.accept(loc, data);
                    }
                }
            }
        }
    }

    private void processResource(String filePath, byte[] data, String fullPrefix,
                                 String fullDir, String namespace, boolean isDirectory,
                                 Set<String> processed, ResourceConsumer consumer) {
        if (isDirectory) {
            int nextSlash = filePath.indexOf('/', fullPrefix.length());
            if (nextSlash != -1 && nextSlash < filePath.length() - 1) {
                String relPath = filePath.substring(fullPrefix.length(), nextSlash + 1);
                if (!processed.contains(relPath)) {
                    processed.add(relPath);
                    ResourceLocation loc = ResourceLocation.tryBuild(namespace, relPath);
                    if (loc != null) {
                        consumer.accept(loc, data);
                    }
                }
            }
        } else if (filePath.startsWith(fullDir)) {
            String relPath = filePath.substring(fullPrefix.length());
            if (!processed.contains(relPath)) {
                processed.add(relPath);
                ResourceLocation loc = ResourceLocation.tryBuild(namespace, relPath);
                if (loc != null) {
                    consumer.accept(loc, data);
                }
            }
        }
    }

    @Override
    public void clear() {
        memoryCache.clear();
        lazyPathIndex.clear();
        namespaceCache.clear();
        resourceIndex.clear();

        try {
            if (zipFileSystem.isOpen()) {
                zipFileSystem.close();
            }
        } catch (IOException ignored) {}
    }

    private String normalizePath(Path path) {
        String str = path.toString();
        return str.startsWith("/") ? str.substring(1) : str;
    }

    private static class CachedResourceEntry {
        final String overlayKey;
        byte[] data;

        CachedResourceEntry(String overlayKey, byte[] data) {
            this.overlayKey = overlayKey;
            this.data = data;
        }
    }

    private static class LruMemoryCache extends LinkedHashMap<String, byte[]> {
        private final int maxSize;

        LruMemoryCache(int maxSize) {
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
            int firstSlash = path.indexOf('/');
            if (firstSlash == -1) return false;
            String firstPart = path.substring(0, firstSlash);
            return !firstPart.isEmpty() && firstPart.indexOf('/') == -1;
        }
    }
}