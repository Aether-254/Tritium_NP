package org.craftamethyst.tritium.integration.embeddium;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.embeddedt.embeddium.api.options.OptionIdentifier;
import org.embeddedt.embeddium.api.options.structure.OptionGroup;
import org.embeddedt.embeddium.api.options.structure.OptionPage;

public class TritiumEmbPage extends OptionPage {

    TritiumEmbPage(String pageId, Component title, ImmutableList<OptionGroup> groups) {
        super(OptionIdentifier.create(
                        ResourceLocation.fromNamespaceAndPath(TritiumCommon.MOD_ID, pageId)),
                title,
                groups);
    }

    public static TritiumEmbPage createPerformancePage() {
        return new TritiumPageBuilder("performance", Component.translatable("config.tritium.category.performance"))
                .addGroup("performance_group", builder -> builder
                        .addBoolean("fast_furnace",
                                "config.tritium.performance.fastFurnace_fastFurnace",
                                (c, v) -> TritiumConfigBase.Performance.FastFurnace.fastFurnace = v,
                                c -> TritiumConfigBase.Performance.FastFurnace.fastFurnace)
                        .addBoolean("block_state_cache",
                                "config.tritium.performance.blockStateCache_blockStatePairKeyCache",
                                (c, v) -> TritiumConfigBase.Performance.BlockStateCache.blockStatePairKeyCache = v,
                                c -> TritiumConfigBase.Performance.BlockStateCache.blockStatePairKeyCache)
                        .addBoolean("lighting_optimizations",
                                "config.tritium.performance.lightingOptimizations_enableLightingOptimizations",
                                (c, v) -> TritiumConfigBase.Performance.LightingOptimizations.enableLightingOptimizations = v,
                                c -> TritiumConfigBase.Performance.LightingOptimizations.enableLightingOptimizations))
                .build();
    }

    public static TritiumEmbPage createRenderingPage() {
        return new TritiumPageBuilder("rendering", Component.translatable("config.tritium.category.rendering"))
                .addGroup("rendering_group", builder -> builder
                        .addBoolean("chest_rendering_opt",
                                "config.tritium.rendering.cro_chest_rendering_optimization",
                                (c, v) -> TritiumConfigBase.Rendering.CRO.chest_rendering_optimization = v,
                                c -> TritiumConfigBase.Rendering.CRO.chest_rendering_optimization)
                        .addBoolean("fast_blit",
                                "config.tritium.rendering.fastBlit_fastBlit",
                                (c, v) -> TritiumConfigBase.Rendering.FastBlit.fastBlit = v,
                                c -> TritiumConfigBase.Rendering.FastBlit.fastBlit)
                        .addBoolean("gpu_plus",
                                "config.tritium.rendering.GpuPlus_gpuPlus",
                                (c, v) -> TritiumConfigBase.Rendering.GpuPlus.gpuPlus = v,
                                c -> TritiumConfigBase.Rendering.GpuPlus.gpuPlus)
                        .addBoolean("gpu_plus_vbo",
                                "config.tritium.rendering.GpuPlus_gpuPlusVbo",
                                (c, v) -> TritiumConfigBase.Rendering.GpuPlus.gpuPlusVbo = v,
                                c -> TritiumConfigBase.Rendering.GpuPlus.gpuPlusVbo)
                        .addBoolean("gpu_plus_index",
                                "config.tritium.rendering.GpuPlus_gpuPlusIndex",
                                (c, v) -> TritiumConfigBase.Rendering.GpuPlus.gpuPlusIndex = v,
                                c -> TritiumConfigBase.Rendering.GpuPlus.gpuPlusIndex)
                        .addBoolean("enable_reflex",
                                "config.tritium.rendering.reflex_enableReflex",
                                (c, v) -> TritiumConfigBase.Rendering.Reflex.enableReflex = v,
                                c -> TritiumConfigBase.Rendering.Reflex.enableReflex)
                        .addInteger("reflex_offset",
                                "config.tritium.rendering.reflex_reflexOffsetNs",
                                -100000, 100000, 1000,
                                v -> Component.literal(v + " ns"),
                                (c, v) -> TritiumConfigBase.Rendering.Reflex.reflexOffsetNs = v,
                                c -> TritiumConfigBase.Rendering.Reflex.reflexOffsetNs)
                        .addBoolean("enable_entity_culling",
                                "config.tritium.rendering.entityCulling_enableCulling",
                                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableCulling = v,
                                c -> TritiumConfigBase.Rendering.EntityCulling.enableCulling)
                        .addBoolean("enable_block_entity_culling",
                                "config.tritium.rendering.entityCulling_enableBlockEntityCulling",
                                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableBlockEntityCulling = v,
                                c -> TritiumConfigBase.Rendering.EntityCulling.enableBlockEntityCulling)
                        .addBoolean("enable_leaf_culling",
                                "config.tritium.rendering.leafCulling_enableLeafCulling",
                                (c, v) -> TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling = v,
                                c -> TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling))
                .build();
    }

    public static TritiumEmbPage createClientOptimizationsPage() {
        return new TritiumPageBuilder("client_optimizations", Component.translatable("config.tritium.category.clientOptimizations"))
                .addGroup("client_optimizations_group", builder -> builder
                        .addBoolean("fast_language",
                                "config.tritium.clientOptimizations.FL_fastLanguageSwitch",
                                (c, v) -> TritiumConfigBase.ClientOptimizations.FL.fastLanguageSwitch = v,
                                c -> TritiumConfigBase.ClientOptimizations.FL.fastLanguageSwitch)
                        .addBoolean("resource_pack_cache",
                                "config.tritium.clientOptimizations.FastResourcePack_resourcePackCache",
                                (c, v) -> TritiumConfigBase.ClientOptimizations.FastResourcePack.resourcePackCache = v,
                                c -> TritiumConfigBase.ClientOptimizations.FastResourcePack.resourcePackCache)
                        .addBoolean("dynamic_fps",
                                "config.tritium.clientOptimizations.dynamicFPS_enable",
                                (c, v) -> TritiumConfigBase.ClientOptimizations.DynamicFPS.enable = v,
                                c -> TritiumConfigBase.ClientOptimizations.DynamicFPS.enable)
                        .addInteger("minimized_fps",
                                "config.tritium.clientOptimizations.dynamicFPS_minimizedFPS",
                                1, 60, 1,
                                v -> v > 1 ? Component.literal(v + " FPS") : Component.literal("1 FPS"),
                                (c, v) -> TritiumConfigBase.ClientOptimizations.DynamicFPS.minimizedFPS = v,
                                c -> TritiumConfigBase.ClientOptimizations.DynamicFPS.minimizedFPS))
                .build();
    }

    public static TritiumEmbPage createEntitiesPage() {
        return new TritiumPageBuilder("entities", Component.translatable("config.tritium.category.entities"))
                .addGroup("entities_group", builder -> builder
                        .addBoolean("optimize_entities",
                                "config.tritium.entities.entityOpt_optimizeEntities",
                                (c, v) -> TritiumConfigBase.Entities.EntityOpt.optimizeEntities = v,
                                c -> TritiumConfigBase.Entities.EntityOpt.optimizeEntities)
                        .addInteger("entity_horizontal_range",
                                "config.tritium.entities.entityOpt_horizontalRange",
                                1, 256, 1,
                                v -> Component.literal(v + " blocks"),
                                (c, v) -> TritiumConfigBase.Entities.EntityOpt.horizontalRange = v,
                                c -> TritiumConfigBase.Entities.EntityOpt.horizontalRange)
                        .addInteger("entity_vertical_range",
                                "config.tritium.entities.entityOpt_verticalRange",
                                1, 256, 1,
                                v -> Component.literal(v + " blocks"),
                                (c, v) -> TritiumConfigBase.Entities.EntityOpt.verticalRange = v,
                                c -> TritiumConfigBase.Entities.EntityOpt.verticalRange)
                        .addBoolean("entity_stacking",
                                "config.tritium.entities.entityStacking_enable",
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.enable = v,
                                c -> TritiumConfigBase.Entities.EntityStacking.enable)
                        .addInteger("max_stack_size",
                                "config.tritium.entities.entityStacking_maxStackSize",
                                0, 100, 1,
                                v -> v > 0 ? Component.literal(v + " entities") : CommonComponents.OPTION_OFF,
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.maxStackSize = v,
                                c -> TritiumConfigBase.Entities.EntityStacking.maxStackSize)
                        .addBoolean("lock_maxed_stacks",
                                "config.tritium.entities.entityStacking_lockMaxedStacks",
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.lockMaxedStacks = v,
                                c -> TritiumConfigBase.Entities.EntityStacking.lockMaxedStacks))
                .build();
    }

    public static TritiumEmbPage createFixesPage() {
        return new TritiumPageBuilder("fixes", Component.translatable("config.tritium.category.fixes"))
                .addGroup("fixes_group", builder -> builder
                        .addBoolean("button_fix",
                                "config.tritium.fixes.buttonFix_buttonFix",
                                (c, v) -> TritiumConfigBase.Fixes.ButtonFix.buttonFix = v,
                                c -> TritiumConfigBase.Fixes.ButtonFix.buttonFix)
                        .addBoolean("no_glog",
                                "config.tritium.fixes.noGLog_noGLog",
                                (c, v) -> TritiumConfigBase.Fixes.NoGLog.noGLog = v,
                                c -> TritiumConfigBase.Fixes.NoGLog.noGLog)
                        .addBoolean("memory_leak_fix_ae2wt",
                                "config.tritium.fixes.memoryLeakFix_AE2WTLibCreativeTabLeakFix",
                                (c, v) -> TritiumConfigBase.Fixes.MemoryLeakFix.AE2WTLibCreativeTabLeakFix = v,
                                c -> TritiumConfigBase.Fixes.MemoryLeakFix.AE2WTLibCreativeTabLeakFix)
                        .addBoolean("bee_fixes",
                                "config.tritium.fixes.beeFixes_enableBeeFixes",
                                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes = v,
                                c -> TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes))
                .build();
    }

    public static TritiumEmbPage createServerPerformancePage() {
        return new TritiumPageBuilder("server_performance", Component.translatable("config.tritium.category.serverPerformance"))
                .addGroup("server_performance_group", builder -> builder
                        .addBoolean("noise_sampling_cache",
                                "config.tritium.serverPerformance.noiseSamplingCache_noiseSamplingCache",
                                (c, v) -> TritiumConfigBase.ServerPerformance.NoiseSamplingCache.noiseSamplingCache = v,
                                c -> TritiumConfigBase.ServerPerformance.NoiseSamplingCache.noiseSamplingCache)
                        .addBoolean("jigsaw_optimizations",
                                "config.tritium.serverPerformance.jigsawOptimizations_enableJigsawOptimizations",
                                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations = v,
                                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations))
                .build();
    }

    public static TritiumEmbPage createTechOptimizationsPage() {
        return new TritiumPageBuilder("tech_optimizations", Component.translatable("config.tritium.category.techOptimizations"))
                .addGroup("tech_optimizations_group", builder -> builder
                        .addBoolean("create_rail_offloading",
                                "config.tritium.techOptimizations.createOptimizations_enableRailOffloading",
                                (c, v) -> TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading = v,
                                c -> TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading))
                .build();
    }

    public static TritiumEmbPage createNetworkPage() {
        return new TritiumPageBuilder("network", Component.translatable("config.tritium.category.network"))
                .addGroup("network_group", builder -> {})
                .build();
    }
}