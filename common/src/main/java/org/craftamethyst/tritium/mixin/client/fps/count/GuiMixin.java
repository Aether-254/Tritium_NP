/*
 * // Copyright (c) 2025 ZCRAFT. Tritium Project. Licensed under MIT.
 */

package org.craftamethyst.tritium.mixin.client.fps.count;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.craftamethyst.tritium.client.fps.FPSCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRenderAfterChat(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker, CallbackInfo ci) {
        tritium$renderFPSCounter(pGuiGraphics);
    }

    @Unique
    private void tritium$renderFPSCounter(GuiGraphics pGuiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        FPSCounter.getInstance().render(pGuiGraphics, font, width, height);
    }
}