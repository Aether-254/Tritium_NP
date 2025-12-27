package org.craftamethyst.tritium.util.resources;

import net.minecraft.server.packs.PackResources;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ResourcePackFactory {
    public static PackResources createCachedPack(Path zip, boolean lazy) throws IOException {
        ZipResourceCache cache = new ZipResourceCache(
                zip,
                List.of(),
                lazy
        );
        return new AbstractCachedPackResources(zip.toString(), false, cache) {};
    }

    public static PackResources createSmartPack(Path zip) {
        try {
            long size = java.nio.file.Files.size(zip);
            boolean lazy = size > 10 * 1024 * 1024;
            return createCachedPack(zip, lazy);
        } catch (IOException e) {
            return new net.minecraft.server.packs.FilePackResources(
                    zip.toFile().getName(),
                    zip.toFile(),
                    false
            );
        }
    }
}