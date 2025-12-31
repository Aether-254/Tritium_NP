package org.craftamethyst.tritium.mixin.packs;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.craftamethyst.tritium.util.resources.ResourcePackFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(FilePackResources.FileResourcesSupplier.class)
public abstract class FileResourcesSupplierMixin {
    @Shadow @Final private File content;

    /**
     * @author ZCRAFT
     * @reason Memory-mapped resource pack loading
     */
    @Overwrite
    public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
        try {
            return ResourcePackFactory.createOptimizedPack(
                    packLocationInfo,
                    content.toPath(),
                    metadata.overlays()
            );
        } catch (Exception e) {
            return new FilePackResources.FileResourcesSupplier(content)
                    .openFull(packLocationInfo, metadata);
        }
    }
}