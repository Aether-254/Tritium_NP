package org.craftamethyst.tritium.mixin.client.renderer.culling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.cull.LeafCullingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class LeafRenderMixin {
    @Inject(
            method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onShouldRenderFace(
            BlockState currentFace,
            BlockState neighboringFace,
            Direction face,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling) {
            return;
        }

        if (currentFace.getBlock() instanceof LeavesBlock) {
            LeafCullingContext.RenderContext context = LeafCullingContext.getCurrentRenderContext();

            if (context != null) {
                BlockPos pos = context.currentPos();
                BlockGetter level = context.level();

                if (pos != null && level != null) {
                    if (TritiumConfigBase.Rendering.LeafCulling.hideInnerLeaves &&
                            LeafCullingContext.shouldHideInnerLeaves(level, pos)) {
                        cir.setReturnValue(false);
                        return;
                    }

                    boolean cull;
                    if (TritiumConfigBase.Rendering.LeafCulling.enableFaceOcclusionCulling) {
                        cull = LeafCullingContext.shouldCullBlockFace(level, pos, face);
                    } else {
                        cull = LeafCullingContext.shouldCullFace(level, pos, face);
                    }
                    cir.setReturnValue(!cull);
                }
            }
        }
    }
}