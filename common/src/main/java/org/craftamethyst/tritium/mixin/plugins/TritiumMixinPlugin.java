// Copyright (c) 2025 CraftAmethyst. Tritium Project. Licensed under MIT.

package org.craftamethyst.tritium.mixin.plugins;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class TritiumMixinPlugin implements IMixinConfigPlugin {
    private static final Set<String> BLACK_LIST = Set.of(
            "org.craftamethyst.tritium.mixin.client.renderer.vertex.VertexBufferMixin"
    );

    private static final Set<String> CONFLICT_MODS = Set.of(
            "immediatelyfast",
            "entity_texture_features"
    );

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        System.out.println("[Tritium Mixin Plugin] Checking: " + mixinClassName);

        if (BLACK_LIST.contains(mixinClassName)) {
            System.out.println("[Tritium Mixin Plugin] Mixin is in blacklist");
            for (String modId : CONFLICT_MODS) {
                boolean loaded = isModLoaded(modId);
                System.out.println("[Tritium Mixin Plugin] Checking mod " + modId + ": " + loaded);
                if (loaded) {
                    System.out.println("[Tritium Mixin Plugin] Conflict mod found, skipping mixin");
                    return false;
                }
            }
            return true;
        }
        return true;
    }
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) { }

    private static boolean isModLoaded(String modId) {
        try {
            if ("immediatelyfast".equals(modId)) {
                Class.forName("net.raphimc.immediatelyfast.ImmediatelyFast");
                System.out.println("[Tritium] ImmediatelyFast class found via Class.forName");
                return true;
            } else if ("entity_texture_features".equals(modId)) {
                try {
                    Class.forName("traben.entity_texture_features.ETF");
                    System.out.println("[Tritium] ETF class found via Class.forName (ETFVersion)");
                    return true;
                } catch (ClassNotFoundException e1) {
                        }
            }
            boolean loaded = org.craftamethyst.tritium.platform.Services.PLATFORM.isModLoaded(modId);
            System.out.println("[Tritium] ModList detection for " + modId + ": " + loaded);
            return loaded;

        } catch (Exception e) {
            System.out.println("[Tritium] Error checking mod " + modId + ": " + e.getMessage());
            return org.craftamethyst.tritium.platform.Services.PLATFORM.isModLoaded(modId);
        }
    }
}