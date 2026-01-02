package me.zcraft.tconfig.client;

import me.zcraft.tconfig.config.TritiumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.craftamethyst.tritium.TritiumCommon;

@Environment(EnvType.CLIENT)
public class TritiumConfigScreenReg {

    public static void registerConfigScreen() {
        try {
            String modId = FabricLoader.getInstance().getModContainer(TritiumCommon.MOD_ID)
                    .orElseThrow(() -> new IllegalStateException("Mod container not found"))
                    .getMetadata().getId();
            registerConfigScreenInternal(modId, TritiumConfig.getConfig(modId));
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to auto-register config screen for current mod", e);
        }
    }

    public static void registerConfigScreen(String modId) {
        try {
            registerConfigScreenInternal(modId, TritiumConfig.getConfig(modId));
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to register config screen for mod: {}", modId, e);
        }
    }

    public static void registerConfigScreen(TritiumConfig config) {
        try {
            registerConfigScreenInternal(config.getModId(), config);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to register config screen for config: {}", config.getConfigClass().getName(), e);
        }
    }

    private static void registerConfigScreenInternal(String modId, TritiumConfig config) {
        if (config == null) {
            TritiumCommon.LOG.warn("No configuration found for mod: {}. Call TritiumConfig.register() first.", modId);
            return;
        }

        if (!config.isClientEnvironment()) {
            TritiumCommon.LOG.debug("Skipping config screen registration for mod {} in server environment", modId);
            return;
        }

        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            TritiumCommon.LOG.info("Config screen registered for mod: {} (available through ModMenu)", modId);
        } else {
            TritiumCommon.LOG.warn("Mod Menu is not installed; config screen for mod {} won't be available.", modId);
        }
    }
}
