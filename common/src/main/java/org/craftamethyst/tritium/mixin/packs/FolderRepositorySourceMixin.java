package org.craftamethyst.tritium.mixin.packs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import org.craftamethyst.tritium.util.resources.AbstractCachedPackResources;
import org.craftamethyst.tritium.util.resources.ResourcePackFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
        if (AbstractCachedPackResources.file != null) {
            return ResourcePackFactory.createSmartPack(
                    AbstractCachedPackResources.file.toPath()
            );
        }
        return original;
    }
}