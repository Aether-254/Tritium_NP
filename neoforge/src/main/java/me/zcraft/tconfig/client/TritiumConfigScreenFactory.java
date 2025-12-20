/*
 * // Copyright (c) 2025 CraftAmethyst. Tritium Project. Licensed under MIT.
 */

package me.zcraft.tconfig.client;

import me.zcraft.tconfig.config.autoconfig.TritiumAutoConfig;
import me.zcraft.tconfig.config.TritiumConfig;
import net.minecraft.client.gui.screens.Screen;

public class TritiumConfigScreenFactory {
    public static Screen createConfigScreen(Screen parent, TritiumConfig config) {
        return new TritiumAutoConfig(config).createConfigScreen(parent);
    }

    public static Screen createConfigScreen(TritiumConfig config) {
        return new TritiumAutoConfig(config).createConfigScreen(null);
    }
}