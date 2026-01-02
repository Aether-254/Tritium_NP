package org.craftamethyst.tritium.mixin.create.renderbounds;

import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.tritium.util.create.CreateRenderBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrogportBlockEntity.class)
public abstract class FrogportBlockEntityRenderBoundsMixin {
    @Shadow
    public abstract BlockPos getBlockPos();

    @Inject(method = "getRenderBoundingBox", at = @At("RETURN"), cancellable = true, remap = false)
    private void tritium$clampRenderBounds(CallbackInfoReturnable<AABB> cir) {
        AABB local = CreateRenderBounds.frogportBounds(getBlockPos(), 1.0);
        cir.setReturnValue(CreateRenderBounds.clampTo(cir.getReturnValue(), local));
    }
}
