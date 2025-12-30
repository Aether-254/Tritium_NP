package org.craftamethyst.tritium;

import me.zcraft.tconfig.client.TritiumConfigScreenReg;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.craftamethyst.tritium.client.TritiumClient;

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
        TritiumConfigScreenReg.registerConfigScreen(TritiumCommon.MOD_ID);
    }
}
