/*
 *  Copyright (c) 2025 CraftAmethyst. Tritium Project. Licensed under MIT.
 */

package org.craftamethyst.tritium.compat.plugin;

import net.fabricmc.loader.api.FabricLoader;
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
    private static final String SODIUM_MODID = "sodium";

    private static final String VERTEX_BUFFER_MIXIN = "org.craftamethyst.tritium.mixin.client.renderer.vertex.VertexBufferMixin";
    private static final String FAST_BLIT_MIXIN = "org.craftamethyst.tritium.mixin.client.renderer.fast_blit.FastBlit";
    private static final String SODIUM_MIXIN = "org.craftamethyst.tritium.mixin.sodium.SodiumOptionsGUIMixin";
    private static final String SODIUM_ACC_MIXIN = "org.craftamethyst.tritium.mixin.sodium.SodiumOptionsGUIAccessor";

    private Boolean hasImmFast;
    private Boolean hasETF;
    private Boolean hasBBS;
    private Boolean hasSod;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return switch (mixinClassName) {
            case VERTEX_BUFFER_MIXIN -> !shouldDisableVertexBufferMixin();
            case FAST_BLIT_MIXIN -> !shouldDisableFastBlitMixin();
            case SODIUM_MIXIN, SODIUM_ACC_MIXIN -> shouldDisableSodiumMixin();
            default -> true;
        };

    }

    private boolean shouldDisableVertexBufferMixin() {
        if (hasImmFast == null || hasETF == null) {
            hasImmFast = FabricLoader.getInstance().isModLoaded(IMMEDIATELY_FAST_MODID);
            hasETF = FabricLoader.getInstance().isModLoaded(ENTITY_TEXTURE_FEATURES_MODID);

            if (hasImmFast || hasETF) {
                System.out.println("[Tritium Compat] Disabling VertexBufferMixin due to compatibility with: " +
                        (hasImmFast ? "ImmediatelyFast" : "") +
                        (hasETF ? (hasImmFast ? "/ETF" : "ETF") : ""));
            }
        }

        return hasImmFast || hasETF;
    }

    private boolean shouldDisableFastBlitMixin() {
        if (hasBBS == null) {
            hasBBS = FabricLoader.getInstance().isModLoaded(BBS_MODID);

            if (hasBBS) {
                System.out.println("[Tritium Compat] Disabling FastBlit due to compatibility issues with BBS screen recording");
            }
        }

        return hasBBS;
    }

    private boolean shouldDisableSodiumMixin() {
        if (hasSod == null) {
            hasSod = FabricLoader.getInstance().isModLoaded(SODIUM_MODID);

            if (hasSod) {
                System.out.println("[Tritium Compat] Disabling SodiumOptionsGUIMixin");
            }
        }

        return hasSod;
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