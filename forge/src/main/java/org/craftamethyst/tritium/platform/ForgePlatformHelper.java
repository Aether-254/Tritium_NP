package org.craftamethyst.tritium.platform;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.platform.services.IPlatformHelper;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
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
}