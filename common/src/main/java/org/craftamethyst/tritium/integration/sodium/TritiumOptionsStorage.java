package org.craftamethyst.tritium.integration.sodium;

import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import me.zcraft.tconfig.config.TritiumConfig;
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