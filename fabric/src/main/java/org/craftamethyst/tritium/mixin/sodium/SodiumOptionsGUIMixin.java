package org.craftamethyst.tritium.mixin.sodium;

import me.zcraft.tconfig.config.TritiumConfig;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptionsGUI;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.integration.sodium.TritiumSodiumOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SodiumOptionsGUI.class)
public class SodiumOptionsGUIMixin {

    @Inject(method = "<init>(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("TAIL"))
    private void tritium$addOptionPages(CallbackInfo ci) {
        try {
            SodiumOptionsGUI gui = (SodiumOptionsGUI) (Object) this;
            SodiumOptionsGUIAccessor accessor = (SodiumOptionsGUIAccessor) gui;
            List<OptionPage> pages = accessor.getPages();

            TritiumConfig config = TritiumConfig.getConfig(TritiumCommon.MOD_ID);
            TritiumConfigBase configBase = config.get();
            TritiumSodiumOptions sodiumOptions = new TritiumSodiumOptions(configBase);
            pages.addAll(sodiumOptions.createOptionPages());

        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to add Tritium pages to Sodium GUI", e);
        }
    }
}