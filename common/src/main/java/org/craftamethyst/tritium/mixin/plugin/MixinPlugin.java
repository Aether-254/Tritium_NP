package org.craftamethyst.tritium.mixin.plugin;

import me.zcraft.tconfig.config.TritiumConfig;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private static TritiumConfigBase config;

    public static void setConfig(TritiumConfigBase config) {
        MixinPlugin.config = config;
    }

    @Override
    public void onLoad(String mixinPackage) {
        try {
            TritiumConfig.register("tritium", TritiumConfigBase.class);
            TritiumCommon.LOG.info("Tritium mixin plugin loaded with config");
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to load Tritium config", e);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (config == null) {
            return true;
        }
        return shouldApplyMixinInternal(mixinClassName);
    }

    private boolean shouldApplyMixinInternal(String mixinClassName) {
        if (mixinClassName.contains("FileResourcesSupplierMixin")) {
            return TritiumConfigBase.ClientOptimizations.FastResourcePack.resourcePackCache;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}