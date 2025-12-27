package org.craftamethyst.tritium.mixin.sodium;

import net.caffeinemc.mods.sodium.client.gui.SodiumOptionsGUI;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SodiumOptionsGUI.class)
public interface SodiumOptionsGUIAccessor {
    @Accessor("pages")
    List<OptionPage> getPages();
}