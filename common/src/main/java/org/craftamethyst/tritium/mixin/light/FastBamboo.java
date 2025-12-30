package org.craftamethyst.tritium.mixin.light;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skip BamBoo Light
 */
@Mixin(net.minecraft.world.level.block.state.BlockBehaviour.class)
public abstract class FastBamboo {

    @Inject(
            method = "getShadeBrightness",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetShadeBrightness(BlockState state,
                                      BlockGetter level,
                                      BlockPos pos,
                                      CallbackInfoReturnable<Float> cir) {
        if (!TritiumConfigBase.Performance.LightingOptimizations.bambooLight) {
            return;
        }
        if (state.getBlock() instanceof BambooBlock) {
            cir.setReturnValue(1.0F);
        }
    }
}