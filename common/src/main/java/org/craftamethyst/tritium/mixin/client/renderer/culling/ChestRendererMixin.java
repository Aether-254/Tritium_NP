package org.craftamethyst.tritium.mixin.client.renderer.culling;

import net.minecraft.client.renderer.blockentity.ChestRenderer;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * &#064;Author:  KSmc_brigade
 * &#064;Date:  2025/11/9 上午8:30
 */
@Mixin(ChestRenderer.class)
public class ChestRendererMixin {
    @ModifyVariable(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/model/ChestModel;FII)V",
            at = @At(value = "HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyOpenness(float openness) {
        if (!TritiumConfigBase.Rendering.CRO.chest_rendering_optimization) {
            return openness;
        }
        return 0F;
    }
}