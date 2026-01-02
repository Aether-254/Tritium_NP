package org.craftamethyst.tritium.mixin.create.renderbounds;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.tritium.util.create.CreateRenderBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FactoryPanelBlockEntity.class)
public abstract class FactoryPanelBlockEntityRenderBoundsMixin {
    @Shadow
    public abstract BlockPos getBlockPos();

    @Inject(method = "createRenderBoundingBox", at = @At("RETURN"), cancellable = true, remap = false)
    private void tritium$clampRenderBounds(CallbackInfoReturnable<AABB> cir) {
        cir.setReturnValue(CreateRenderBounds.clampToLocal(cir.getReturnValue(), getBlockPos(), 2.0));
    }
}
