/*
 * // Copyright (c) 2025 CraftAmethyst. Tritium Project. Licensed under MIT.
 */

package org.craftamethyst.tritium.client;

import me.zcraft.tconfig.client.TritiumConfigScreenReg;
import net.fabricmc.api.ClientModInitializer;
import org.craftamethyst.tritium.TritiumCommon;

public class TritiumFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new TritiumClient();
        TritiumConfigScreenReg.registerConfigScreen(TritiumCommon.MOD_ID);
    }
}
