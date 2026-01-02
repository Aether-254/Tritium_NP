package org.craftamethyst.tritium.mixin.create.renderbounds;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.tritium.util.create.CreateRenderBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltBlockEntity.class)
public abstract class BeltBlockEntityRenderBoundsMixin {
    @Shadow
    public int beltLength;

    @Shadow
    public abstract Vec3i getBeltChainDirection();

    @Shadow
    public abstract boolean isController();

    @Shadow
    public abstract BlockPos getBlockPos();

    @Inject(method = "createRenderBoundingBox", at = @At("RETURN"), cancellable = true, remap = false)
    private void tritium$clampRenderBounds(CallbackInfoReturnable<AABB> cir) {
        if (!isController()) {
            return;
        }
        Vec3i direction = getBeltChainDirection();
        int length = beltLength + 1;
        AABB bounds = CreateRenderBounds.beltBounds(getBlockPos(), direction, length, 1.0);
        cir.setReturnValue(bounds);
    }
}
