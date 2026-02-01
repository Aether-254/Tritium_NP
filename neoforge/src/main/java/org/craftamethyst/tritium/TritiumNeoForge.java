package org.craftamethyst.tritium;

import me.zcraft.tconfig.client.TritiumConfigScreenReg;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.client.fps.FPSCounter;
import org.craftamethyst.tritium.integration.embeddium.TritiumEmbIntegration;

@Mod(TritiumCommon.MOD_ID)
public class TritiumNeoForge {

    public TritiumNeoForge(IEventBus modEventBus) {
        TritiumCommon.init();
        modEventBus.addListener(this::onClientSideSetup);
    }

    private static boolean isEmbLoaded() {
        return ModList.get().isLoaded("embeddium");
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
}