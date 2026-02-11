package org.craftamethyst.tritium.mixin.client.renderer.culling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.craftamethyst.tritium.cull.LeafCullingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @Inject(
            method = "tesselateBlock",
            at = @At("HEAD")
    )
    private void tritium$onTesselateBlockStart(
            BlockAndTintGetter level,
            List<BlockModelPart> parts,
            BlockState state,
            BlockPos pos,
            PoseStack poseStack,
            VertexConsumer consumer,
            boolean checkSides,
            int packedOverlay,
            CallbackInfo ci
    ) {
        if (checkSides) {
            LeafCullingContext.setCurrentRenderContext(
                    new LeafCullingContext.RenderContext(pos, level)
            );
        }
    }

    @Inject(
            method = "tesselateBlock",
            at = @At("RETURN")
    )
    private void tritium$onTesselateBlockEnd(
            BlockAndTintGetter level,
            List<BlockModelPart> parts,
            BlockState state,
            BlockPos pos,
            PoseStack poseStack,
            VertexConsumer consumer,
            boolean checkSides,
            int packedOverlay,
            CallbackInfo ci
    ) {
        if (checkSides) {
            LeafCullingContext.clearCurrentRenderContext();
        }
    }
}