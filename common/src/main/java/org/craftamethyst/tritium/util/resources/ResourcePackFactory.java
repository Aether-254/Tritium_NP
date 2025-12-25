package org.craftamethyst.tritium.util.resources;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ResourcePackFactory {

    public static PackResources createCachedPack(PackLocationInfo info,
                                                 Path zipPath,
                                                 List<String> overlays) throws IOException {
        ZipResourceCache cache = new ZipResourceCache(
                zipPath,
                overlays,
                new ZipResourceCache.DefaultOverlayStrategy()
        );

        return new AbstractCachedPackResources(info, cache) {
        };
    }
    public static PackResources createFallbackPack(PackLocationInfo info,
                                                   Path zipPath,
                                                   List<String> overlays,
                                                   Pack.Metadata metadata) {
        try {
            return createCachedPack(info, zipPath, overlays);
        } catch (IOException e) {
            FilePackResources.FileResourcesSupplier supplier =
                    new FilePackResources.FileResourcesSupplier(zipPath.toFile());
            return supplier.openFull(info, metadata);
        }
    }
}