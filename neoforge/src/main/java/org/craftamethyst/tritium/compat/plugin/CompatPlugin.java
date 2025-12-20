/*
 *  Copyright (c) 2025 CraftAmethyst. Tritium Project. Licensed under MIT.
 */

package org.craftamethyst.tritium.compat.plugin;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class CompatPlugin implements IMixinConfigPlugin {
    private static final String IMMEDIATELY_FAST_MODID = "immediatelyfast";
    private static final String ENTITY_TEXTURE_FEATURES_MODID = "entity_texture_features";
    private static final String BBS_MODID = "bbs";

    private static final String VERTEX_BUFFER_MIXIN = "org.craftamethyst.tritium.mixin.client.renderer.vertex.VertexBufferMixin";
    private static final String FAST_BLIT_MIXIN = "org.craftamethyst.tritium.mixin.client.renderer.fast_blit.FastBlit";

    private Boolean hasImmFast;
    private Boolean hasETF;
    private Boolean hasBBS;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals(VERTEX_BUFFER_MIXIN)) {
            return !shouldDisableVertexBufferMixin();
        }

        if (mixinClassName.equals(FAST_BLIT_MIXIN)) {
            return !shouldDisableFastBlitMixin();
        }

        return true;
    }

    private boolean shouldDisableVertexBufferMixin() {
        if (hasImmFast == null || hasETF == null) {
            hasImmFast = FMLLoader.getLoadingModList().getModFileById(IMMEDIATELY_FAST_MODID) != null;
            hasETF = FMLLoader.getLoadingModList().getModFileById(ENTITY_TEXTURE_FEATURES_MODID) != null;

            if (hasImmFast || hasETF) {
                System.out.println("[TritiumMixinPlugin] Is ImmediaitlyFast/ETF endable?: " + VERTEX_BUFFER_MIXIN);
            } else {
                System.out.println("[TritiumMixinPlugin] Can't Find ImmediaitlyFast/ETF,Mixin load: " + VERTEX_BUFFER_MIXIN);
            }
        }

        return hasImmFast || hasETF;
    }

    private boolean shouldDisableFastBlitMixin() {
        if (hasBBS == null) {
            hasBBS = FMLLoader.getLoadingModList().getModFileById(BBS_MODID) != null;

            if (hasBBS) {
                System.out.println("[TritiumMixinPlugin] BBS mod detected, disabling FastBlit for screen recording compatibility");
            }
        }

        return hasBBS;
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