package org.craftamethyst.tritium.mixin.client.fps.count;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import org.craftamethyst.tritium.client.fps.FPSCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin extends GuiComponent {
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRenderAfterChat(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        tritium$renderFPSCounter(poseStack);
    }

    @Unique
    private void tritium$renderFPSCounter(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        FPSCounter.getInstance().render(poseStack, font, width, height);
    }
}