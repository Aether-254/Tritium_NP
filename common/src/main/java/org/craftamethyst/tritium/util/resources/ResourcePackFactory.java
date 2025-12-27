package org.craftamethyst.tritium.util.resources;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ResourcePackFactory {

    public static PackResources createSmartPack(PackLocationInfo info,
                                                Path zipPath,
                                                List<String> overlays) throws IOException {
        boolean useLazy = shouldUseLazyLoading(zipPath);
        int cacheSize = calculateOptimalCacheSize(zipPath);

        ZipResourceCache cache = new ZipResourceCache(
                zipPath,
                overlays,
                new ZipResourceCache.DefaultOverlayStrategy(),
                useLazy,
                cacheSize
        );

        return new AbstractCachedPackResources(info, cache) {};
    }

    public static PackResources createFallbackPack(PackLocationInfo info,
                                                   Path zipPath,
                                                   List<String> overlays,
                                                   Pack.Metadata metadata) {
        try {
            return createSmartPack(info, zipPath, overlays);
        } catch (IOException e) {
            return new FilePackResources.FileResourcesSupplier(zipPath.toFile())
                    .openFull(info, metadata);
        }
    }

    private static boolean shouldUseLazyLoading(Path zipPath) throws IOException {
        long size = Files.size(zipPath);
        return size > 50 * 1024 * 1024;
    }

    private static int calculateOptimalCacheSize(Path zipPath) throws IOException {
        long size = Files.size(zipPath);
        if (size < 10 * 1024 * 1024) return 4096;
        if (size < 100 * 1024 * 1024) return 8192;
        return 16384;
    }
}