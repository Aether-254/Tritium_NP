package org.craftamethyst.tritium.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.platform.services.IPlatformHelper;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        ModList modList = ModList.get();
        if (modList == null) {
            return false;
        }
        return modList.isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isClientEnvironment() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public String getModVersion() {
        return ModList.get()
                .getModContainerById(TritiumCommon.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("Unknown");
    }

    @Override
    public boolean isCreateLoaded() {
        return ModList.get().isLoaded("create");
    }
}