package org.craftamethyst.tritium.mixin.client.lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.util.lang.LanguageLoadOptimizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageManager.class)
public abstract class LanguageManagerMixin {

    @Shadow
    private String currentCode;
    @Unique
    private String tritium$previousLanguage;

    @Shadow
    protected abstract void onResourceManagerReload(ResourceManager resourceManager);

    @Inject(method = "setSelected", at = @At("HEAD"))
    private void onSetLanguageHead(String languageCode, CallbackInfo ci) {
        if (!TritiumConfigBase.ClientOptimizations.FL.fastLanguageSwitch) {
            return;
        }

        tritium$previousLanguage = this.currentCode;

        if (tritium$previousLanguage != null && !tritium$previousLanguage.equals(languageCode)) {
            LanguageLoadOptimizer.setLanguageChanging(true);
        }
    }

    @Inject(method = "setSelected", at = @At("TAIL"))
    private void onSetLanguageTail(String languageCode, CallbackInfo ci) {
        if (!TritiumConfigBase.ClientOptimizations.FL.fastLanguageSwitch) {
            return;
        }

        if (tritium$previousLanguage != null && !tritium$previousLanguage.equals(languageCode)) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.getResourceManager() != null) {
                try {
                    this.onResourceManagerReload(minecraft.getResourceManager());
                } catch (Exception e) {
                    TritiumCommon.LOG.error("Failed to reload language resources", e);
                    LanguageLoadOptimizer.reset();
                }
            }
        }
    }
}
