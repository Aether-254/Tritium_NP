/*
 * // Copyright (c) 2025 ZCRAFT. Tritium Project. Licensed under MIT.
 */

package org.craftamethyst.tritium.mixin.client.fps.minecraft;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.main.GameConfig;
import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.client.fps.FPSCounter;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Final
    private Window window;

    @Shadow
    public abstract boolean isWindowActive();

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void init(GameConfig pGameConfig, CallbackInfo ci) {
        GLFW.glfwSetWindowIconifyCallback(this.window.getWindow(), (window, iconified) -> {
            if (TritiumConfigBase.ClientOptimizations.DynamicFPS.enable) Minecraft.getInstance().noRender = iconified;
        });
    }

    @Inject(method = "getFramerateLimitTracker", at = @At("RETURN"), cancellable = true)
    public void framerateLimit(CallbackInfoReturnable<Integer> cir) {
        if (!this.isWindowActive() && TritiumConfigBase.ClientOptimizations.DynamicFPS.enable) {
            cir.setReturnValue(TritiumConfigBase.ClientOptimizations.DynamicFPS.minimizedFPS);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tritium$culling(CallbackInfo ci) {
        if (TritiumClient.instance != null) {
            TritiumClient.instance.clientTick();
        }
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onRunTickHead(CallbackInfo ci) {
        FPSCounter.getInstance().update();
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onSetScreen(Screen newScreen, CallbackInfo ci) {
        if (newScreen instanceof TitleScreen) {
            FPSCounter.getInstance().resetHistory();
        }
    }
}
