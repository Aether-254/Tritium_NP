package org.craftamethyst.tritium;

import me.zcraft.tconfig.client.TritiumConfigScreenReg;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.craftamethyst.tritium.command.KillMobsCommand;
import org.craftamethyst.tritium.integration.embeddium.TritiumEmbIntegration;

@Mod(TritiumCommon.MOD_ID)
public class TritiumNeoForge {

    public TritiumNeoForge(IEventBus modEventBus) {
        TritiumCommon.init();
        modEventBus.addListener(this::onClientSideSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    public void onClientSideSetup(FMLClientSetupEvent event) {
        System.out.println("Client setup for Tritium");
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
    private void onRegisterCommands(RegisterCommandsEvent event) {
        KillMobsCommand.register(event.getDispatcher());
    }
}