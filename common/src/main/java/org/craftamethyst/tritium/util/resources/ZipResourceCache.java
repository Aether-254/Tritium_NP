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

public class ZipResourceCache implements IResourceCache {
    private final Map<String, byte[]> fileCache = new ConcurrentHashMap<>();
    private final Map<PackType, Set<String>> namespaceCache = new EnumMap<>(PackType.class);
    private final FileSystem zipFileSystem;
    private final OverlayStrategy overlayStrategy;
    private final String[] overlayPrefixes;
    private final Map<String, List<CachedResourceEntry>> resourceIndex = new ConcurrentHashMap<>();

    public ZipResourceCache(Path zipPath, List<String> overlays,
                            OverlayStrategy strategy) throws IOException {
        this.zipFileSystem = FileSystems.newFileSystem(zipPath);
        this.overlayStrategy = strategy;
        this.overlayPrefixes = buildOptimizedOverlayPrefixes(overlays);

        for (PackType type : PackType.values()) {
            namespaceCache.put(type, ConcurrentHashMap.newKeySet());
        }

        loadResources();
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

    private void loadResources() throws IOException {
        ResourceIndexer indexer = new ResourceIndexer(namespaceCache, overlayStrategy);

        Files.walk(zipFileSystem.getPath("/"))
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    try {
                        String relativePath = normalizePath(filePath);
                        byte[] data = Files.readAllBytes(filePath);
                        fileCache.put(relativePath, data);

                        indexResource(relativePath, data);
                        indexer.indexPath(relativePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
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

        if (entries != null) {
            for (String prefix : overlayPrefixes) {
                String overlayKey = prefix.isEmpty() ? "" : prefix.substring(0, prefix.length() - 1);
                for (CachedResourceEntry entry : entries) {
                    if (entry.overlayKey.equals(overlayKey)) {
                        return entry.data;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public byte @Nullable [] getRootResource(String... parts) {
        String path = String.join("/", parts);
        return fileCache.get(path);
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

            for (Map.Entry<String, byte[]> entry : fileCache.entrySet()) {
                String filePath = entry.getKey();
                if (filePath.startsWith(fullPrefix)) {
                    if (path.isEmpty()) {
                        int nextSlash = filePath.indexOf('/', fullPrefix.length());
                        if (nextSlash != -1 && nextSlash < filePath.length() - 1) {
                            String relPath = filePath.substring(fullPrefix.length(), nextSlash + 1);
                            if (!processed.contains(relPath)) {
                                processed.add(relPath);
                                ResourceLocation loc = ResourceLocation.tryBuild(namespace, relPath);
                                if (loc != null) {
                                    consumer.accept(loc, entry.getValue());
                                }
                            }
                        }
                    } else if (filePath.startsWith(fullDir)) {
                        String relPath = filePath.substring(fullPrefix.length());
                        ResourceLocation loc = ResourceLocation.tryBuild(namespace, relPath);
                        if (loc != null && !processed.contains(relPath)) {
                            processed.add(relPath);
                            consumer.accept(loc, entry.getValue());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        fileCache.clear();
        namespaceCache.clear();
        resourceIndex.clear();
        try {
            if (zipFileSystem.isOpen()) {
                zipFileSystem.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String normalizePath(Path path) {
        String str = path.toString();
        return str.startsWith("/") ? str.substring(1) : str;
    }

    private static class CachedResourceEntry {
        final String overlayKey;
        final byte[] data;

        CachedResourceEntry(String overlayKey, byte[] data) {
            this.overlayKey = overlayKey;
            this.data = data;
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