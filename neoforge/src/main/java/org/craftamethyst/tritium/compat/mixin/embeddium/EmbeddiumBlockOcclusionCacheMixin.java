package org.craftamethyst.tritium.compat.mixin.embeddium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import org.craftamethyst.tritium.cull.BlockFaceOcclusionCuller;
import org.craftamethyst.tritium.cull.LeafCulling;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.BlockOcclusionCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(BlockOcclusionCache.class)
public class EmbeddiumBlockOcclusionCacheMixin {

    @Inject(
            method = "shouldDrawSide",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onShouldDrawSide(BlockState selfState, BlockGetter view, BlockPos pos, Direction facing,
                                  CallbackInfoReturnable<Boolean> cir) {
        if (!TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling) {
            return;
        }

        if (!(selfState.getBlock() instanceof LeavesBlock)) {
            return;
        }

        if (TritiumConfigBase.Rendering.LeafCulling.hideInnerLeaves &&
                LeafCulling.shouldHideInnerLeaves(view, pos)) {
            cir.setReturnValue(false);
            return;
        }

        boolean shouldCull;
        if (TritiumConfigBase.Rendering.LeafCulling.enableFaceOcclusionCulling) {
            shouldCull = BlockFaceOcclusionCuller.shouldCullBlockFace(view, pos, facing);
        } else {
            shouldCull = LeafCulling.shouldCullFace(view, pos, facing);
        }
        cir.setReturnValue(!shouldCull);
    }
}