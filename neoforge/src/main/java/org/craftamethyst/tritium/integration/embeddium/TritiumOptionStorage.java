package org.craftamethyst.tritium.integration.embeddium;

import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.embeddedt.embeddium.api.options.structure.OptionStorage;
import me.zcraft.tconfig.config.TritiumConfig;

public class TritiumOptionStorage implements OptionStorage<TritiumConfigBase> {

    private static final TritiumOptionStorage INSTANCE = new TritiumOptionStorage();

    private TritiumOptionStorage() {}

    public static TritiumOptionStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public TritiumConfigBase getData() {
        return TritiumConfig.getConfig(TritiumCommon.MOD_ID).get();
    }

    @Override
    public void save() {
        TritiumConfig.getConfig(TritiumCommon.MOD_ID).save();
    }
}