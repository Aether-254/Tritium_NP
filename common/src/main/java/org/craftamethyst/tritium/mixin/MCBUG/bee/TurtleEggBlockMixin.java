package org.craftamethyst.tritium.mixin.MCBUG.bee;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.TurtleEggBlock;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TurtleEggBlock.class)
public class TurtleEggBlockMixin {

    @Inject(method = "canDestroyEgg", at = @At("HEAD"), cancellable = true)
    private void tritum$insertBeeCheck(ServerLevel p_376510_, Entity p_57769_, CallbackInfoReturnable<Boolean> cir) {
        if (TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes &&
                TritiumConfigBase.Fixes.BeeFixes.fixBeeTurtleEgg) {
            if (p_57769_ instanceof Bee) {
                cir.setReturnValue(false);
            }
        }
    }
}