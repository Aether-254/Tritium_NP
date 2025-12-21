package org.craftamethyst.tritium;

import me.zcraft.tconfig.client.TritiumConfigScreenReg;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.integration.embeddium.TritiumEmbIntegration;

@Mod(TritiumCommon.MOD_ID)
public class TritiumForge {

    public TritiumForge() {
        TritiumCommon.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSideSetup);
    }
    public void onClientSideSetup(FMLClientSetupEvent event) {
        System.out.println("Client setup for Tritium");
        event.enqueueWork(() -> {
            new TritiumClient();
            TritiumCommon.LOG.info("TritiumClient initialized");
        });
        if (FMLEnvironment.dist == Dist.CLIENT) {
            if (isEmbLoaded()) {
                TritiumEmbIntegration.init();
            }
        }
        TritiumConfigScreenReg.registerConfigScreen(TritiumCommon.MOD_ID);
    }
    private static boolean isEmbLoaded() {
        return ModList.get().isLoaded("embeddium");
    }
}
