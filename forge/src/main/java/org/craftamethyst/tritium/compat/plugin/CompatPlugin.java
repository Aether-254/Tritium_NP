/*
 * // Copyright (c) 2025 CraftAmethyst. Tritium Project. Licensed under MIT.
 */

package org.craftamethyst.tritium.compat.plugin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CompatPlugin implements IMixinConfigPlugin {
    private static final String IMMEDIATELY_FAST_MODID = "immediatelyfast";
    private static final String ENTITY_TEXTURE_FEATURES_MODID = "entity_texture_features";

    private static final String TARGET_MIXIN_CLASS = "org.craftamethyst.tritium.mixin.client.renderer.vertex.VertexBufferMixin";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals(TARGET_MIXIN_CLASS)) {
            boolean isImmediatelyFastLoaded = FMLLoader.getLoadingModList().getModFileById(IMMEDIATELY_FAST_MODID) != null;
            boolean isEntityTextureFeaturesLoaded = FMLLoader.getLoadingModList().getModFileById(ENTITY_TEXTURE_FEATURES_MODID) != null;

            if (isImmediatelyFastLoaded || isEntityTextureFeaturesLoaded) {
                System.out.println("[TritiumMixinPlugin] Is ImmediaitlyFast/ETF endable? Can't use this mixin class(qx): " + mixinClassName);
                return false;
            }
            System.out.println("[TritiumMixinPlugin] Can't Find ImmediaitlyFast/ETF,Mixin load: " + mixinClassName);
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