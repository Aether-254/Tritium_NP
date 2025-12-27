package org.craftamethyst.tritium.util.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.Map;
import java.util.Set;

class ResourceIndexer {
    private final Map<PackType, Set<String>> namespaceCache;
    private final ZipResourceCache.OverlayStrategy overlayStrategy;

    ResourceIndexer(Map<PackType, Set<String>> namespaceCache,
                            ZipResourceCache.OverlayStrategy overlayStrategy) {
        this.namespaceCache = namespaceCache;
        this.overlayStrategy = overlayStrategy;
    }

    void indexPath(String path) {
        for (PackType type : PackType.values()) {
            String namespace = extractNamespaceFromPath(path, type);
            if (namespace != null && ResourceLocation.isValidNamespace(namespace)) {
                namespaceCache.get(type).add(namespace);
            }
        }
    }

    private String extractNamespaceFromPath(String path, PackType type) {
        String remainingPath = path;

        if (overlayStrategy.isOverlayPath(path)) {
            int firstSlash = path.indexOf('/');
            if (firstSlash == -1) return null;
            remainingPath = path.substring(firstSlash + 1);
        }

        String typeDir = type.getDirectory() + "/";
        int typeIndex = remainingPath.indexOf(typeDir);
        if (typeIndex == -1) return null;

        String afterType = remainingPath.substring(typeIndex + typeDir.length());
        int namespaceEnd = afterType.indexOf('/');
        return namespaceEnd == -1 ? null : afterType.substring(0, namespaceEnd);
    }
}