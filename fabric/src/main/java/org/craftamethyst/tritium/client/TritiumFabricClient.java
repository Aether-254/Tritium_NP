/*
 * // Copyright (c) 2025 CraftAmethyst. Tritium Project. Licensed under MIT.
 */

package org.craftamethyst.tritium.client;

import net.fabricmc.api.ClientModInitializer;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.TritiumConfigScreenReg;

public class TritiumFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TritiumConfigScreenReg.registerConfigScreen(TritiumCommon.MOD_ID);
    }
}
