package org.craftamethyst.tritium.mixin.create.renderbounds;

import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.tritium.util.create.CreateRenderBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrogportBlockEntity.class)
public abstract class FrogportBlockEntityRenderBoundsMixin {
    @Inject(method = "getRenderBoundingBox", at = @At("RETURN"), cancellable = true, remap = false)
    private void tritium$clampRenderBounds(CallbackInfoReturnable<AABB> cir) {
        BlockPos pos = ((BlockEntity) (Object) this).getBlockPos();
        AABB local = CreateRenderBounds.frogportBounds(pos, 1.0);
        cir.setReturnValue(CreateRenderBounds.clampTo(cir.getReturnValue(), local));
    }
}
