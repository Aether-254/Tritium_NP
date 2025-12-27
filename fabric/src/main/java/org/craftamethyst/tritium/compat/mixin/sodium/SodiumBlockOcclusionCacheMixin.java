package org.craftamethyst.tritium.compat.mixin.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.cull.BlockFaceOcclusionCuller;
import org.craftamethyst.tritium.cull.LeafCulling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(BlockOcclusionCache.class)
public class SodiumBlockOcclusionCacheMixin {

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(
            method = "shouldDrawSide",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void tritium$onShouldDrawSide(
            BlockState state,
            BlockGetter world,
            BlockPos pos,
            Direction face,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling) return;
        if (!(state.getBlock() instanceof LeavesBlock)) return;

        boolean hide = TritiumConfigBase.Rendering.LeafCulling.hideInnerLeaves
                && LeafCulling.shouldHideInnerLeaves(world, pos);
        if (hide) {
            cir.setReturnValue(false);
            return;
        }

        boolean cull = TritiumConfigBase.Rendering.LeafCulling.enableFaceOcclusionCulling
                ? BlockFaceOcclusionCuller.shouldCullBlockFace(world, pos, face)
                : LeafCulling.shouldCullFace(world, pos, face);
        cir.setReturnValue(!cull);
    }
}