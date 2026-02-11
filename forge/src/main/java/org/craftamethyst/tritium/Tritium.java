package org.craftamethyst.tritium;

import me.zcraft.tconfig.client.TritiumConfigScreenReg;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.craftamethyst.tritium.client.TritiumClient;

@Mod(TritiumCommon.MOD_ID)
public class Tritium {

    public Tritium(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        TritiumCommon.init();
        FMLClientSetupEvent.getBus(modBusGroup).addListener(this::onClientSideSetup);
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
