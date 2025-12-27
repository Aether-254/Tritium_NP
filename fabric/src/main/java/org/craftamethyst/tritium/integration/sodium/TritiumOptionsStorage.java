package org.craftamethyst.tritium.integration.sodium;

import me.zcraft.tconfig.config.TritiumConfig;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

public class TritiumOptionsStorage implements OptionStorage<TritiumConfigBase> {
    private final TritiumConfigBase config;

    public TritiumOptionsStorage(TritiumConfigBase config) {
        this.config = config;
    }

    @Override
    public TritiumConfigBase getData() {
        return config;
    }

    @Override
    public void save() {
        TritiumConfig.getConfig(TritiumCommon.MOD_ID).save();
    }
}