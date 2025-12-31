package org.craftamethyst.tritium.mixin.packs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import org.craftamethyst.tritium.util.resources.MappedPackResources;
import org.craftamethyst.tritium.util.resources.ResourcePackFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mixin(FolderRepositorySource.class)
public class FolderRepositorySourceMixin {

    @ModifyReturnValue(
            method = {
                    "lambda$detectPackResources$2",
                    "method_45268",
            },
            at = @At("TAIL")
    )
    private static PackResources tritium$replaceWithSmartPack(PackResources original) {
        if (MappedPackResources.file != null) {
            try {
                Path zipPath = MappedPackResources.file.toPath();
                String fileName = zipPath.getFileName().toString();
                String packId;
                int lastDotIndex = fileName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    packId = fileName.substring(0, lastDotIndex);
                } else {
                    packId = fileName;
                }
                List<String> overlays = new ArrayList<>();

                return ResourcePackFactory.createOptimizedPack(
                        packId,
                        zipPath,
                        overlays
                );
            } catch (IOException e) {
                System.err.println("[Tritium] Failed to create optimized pack: " + e.getMessage());
                return original;
            }
        }
        return original;
    }
}