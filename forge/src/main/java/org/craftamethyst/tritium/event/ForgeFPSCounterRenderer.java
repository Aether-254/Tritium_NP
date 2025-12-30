package org.craftamethyst.tritium.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.client.fps.FPSCounter;

@Mod.EventBusSubscriber(modid = TritiumCommon.MOD_ID, value = Dist.CLIENT)
public class ForgeFPSCounterRenderer {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        FPSCounter.getInstance().render(
                event.getGuiGraphics(),
                Minecraft.getInstance().font,
                event.getWindow().getGuiScaledWidth(),
                event.getWindow().getGuiScaledHeight()
        );
    }
}