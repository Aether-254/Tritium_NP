package org.craftamethyst.tritium.util.resources;

import net.minecraft.server.packs.PackType;
import java.util.Map;
import java.util.Set;

class ResourceIndexer {
    private static final boolean[] VALID_NS_CHARS = new boolean[256];
    static {
        for (int i = 'a'; i <= 'z'; i++) VALID_NS_CHARS[i] = true;
        for (int i = '0'; i <= '9'; i++) VALID_NS_CHARS[i] = true;
        VALID_NS_CHARS['_'] = true;
        VALID_NS_CHARS['-'] = true;
        VALID_NS_CHARS['.'] = true;
    }

    private final Map<PackType, Set<String>> namespaceCache;
    private final ZipResourceCache.OverlayStrategy overlayStrategy;

    ResourceIndexer(Map<PackType, Set<String>> namespaceCache, ZipResourceCache.OverlayStrategy overlayStrategy) {
        this.namespaceCache = namespaceCache;
        this.overlayStrategy = overlayStrategy;
    }

    void indexPath(String path) {
        for (PackType type : PackType.values()) {
            String ns = extractNamespace(path, type);
            if (ns != null && isValidNamespace(ns)) {
                namespaceCache.get(type).add(ns);
            }
        }
    }

    private String extractNamespace(String path, PackType type) {
        String work = path;
        if (overlayStrategy.isOverlayPath(path)) {
            int slash = path.indexOf('/');
            if (slash == -1) return null;
            work = path.substring(slash + 1);
        }
        String prefix = type.getDirectory() + "/";
        int idx = work.indexOf(prefix);
        if (idx == -1) return null;
        String after = work.substring(idx + prefix.length());
        int end = after.indexOf('/');
        return end == -1 ? null : after.substring(0, end);
    }

    private static boolean isValidNamespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 256 || !VALID_NS_CHARS[c]) return false;
        }
        return !s.isEmpty();
    }
}