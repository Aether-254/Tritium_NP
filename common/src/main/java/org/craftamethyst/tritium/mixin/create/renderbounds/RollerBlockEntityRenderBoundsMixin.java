package org.craftamethyst.tritium.mixin.create.renderbounds;

import com.simibubi.create.content.contraptions.actors.roller.RollerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.tritium.util.create.CreateRenderBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RollerBlockEntity.class)
public abstract class RollerBlockEntityRenderBoundsMixin {
    @Shadow
    public abstract BlockPos getBlockPos();

    @Inject(method = "createRenderBoundingBox", at = @At("RETURN"), cancellable = true, remap = false)
    private void tritium$clampRenderBounds(CallbackInfoReturnable<AABB> cir) {
        cir.setReturnValue(CreateRenderBounds.clampToLocal(cir.getReturnValue(), getBlockPos(), 1.0));
    }
}
