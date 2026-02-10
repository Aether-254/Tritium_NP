package org.craftamethyst.tritium.mixin.MCBUG.boat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractBoat.class)
public abstract class BoatMixin {

    @Inject(
            method = "checkFallDamage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCheckFallDamage(double p_376661_, boolean p_376924_, BlockState p_376918_, BlockPos p_376727_, CallbackInfo ci) {
        AbstractBoat boat = (AbstractBoat) (Object) this;
        boat.fallDistance = 0.0F;
        ci.cancel();
    }
}