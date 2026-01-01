package org.craftamethyst.tritium.util.resources;

import net.minecraft.server.packs.PackResources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ResourcePackFactory {

    public static PackResources createOptimizedPack(String id,
                                                    Path zipPath,
                                                    List<String> overlays) throws IOException {
        boolean useLazyMapping = shouldUseLazyMapping(zipPath);
        int cacheSize = determineCacheSize(zipPath);

        MemoryMappedResourceCache cache = new MemoryMappedResourceCache(
                zipPath,
                overlays,
                new MemoryMappedResourceCache.HierarchicalOverlayLayout(overlays),
                useLazyMapping,
                cacheSize
        );

        return new MappedPackResources(id, true, cache);
    }

    private static boolean shouldUseLazyMapping(Path zipPath) throws IOException {
        if (Files.isRegularFile(zipPath) && zipPath.toString().toLowerCase().endsWith(".zip")) {
            return false;
        }
        long size = Files.size(zipPath);
        return size > 20 * 1024 * 1024;
    }


    private static int determineCacheSize(Path zipPath) throws IOException {
        long size = Files.size(zipPath);
        if (size < 5 * 1024 * 1024) return 4096;
        if (size < 50 * 1024 * 1024) return 8192;
        if (size < 200 * 1024 * 1024) return 16384;
        return 32768;
    }
}