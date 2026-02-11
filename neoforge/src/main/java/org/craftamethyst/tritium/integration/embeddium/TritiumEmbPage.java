package org.craftamethyst.tritium.integration.embeddium;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.embeddedt.embeddium.api.options.OptionIdentifier;
import org.embeddedt.embeddium.api.options.control.ControlValueFormatter;
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
                .addGroup("group_fast_furnace", Component.translatable("config.tritium.performance.fastFurnace"), builder -> builder
                        .addBoolean("fast_furnace",
                                "config.tritium.performance.fastFurnace_fastFurnace",
                                (c, v) -> TritiumConfigBase.Performance.FastFurnace.fastFurnace = v,
                                c -> TritiumConfigBase.Performance.FastFurnace.fastFurnace))
                .addGroup("group_block_state_cache", Component.translatable("config.tritium.performance.blockStateCache"), builder -> builder
                        .addBoolean("block_state_cache",
                                "config.tritium.performance.blockStateCache_blockStatePairKeyCache",
                                (c, v) -> TritiumConfigBase.Performance.BlockStateCache.blockStatePairKeyCache = v,
                                c -> TritiumConfigBase.Performance.BlockStateCache.blockStatePairKeyCache))
                .addGroup("lighting_optimizations", Component.translatable("config.tritium.performance.lightingOptimizations"), builder -> builder
                        .addBoolean("enable_lighting_optimizations",
                                "config.tritium.performance.lightingOptimizations_enableLightingOptimizations",
                                (c, v) -> TritiumConfigBase.Performance.LightingOptimizations.enableLightingOptimizations = v,
                                c -> TritiumConfigBase.Performance.LightingOptimizations.enableLightingOptimizations)
                        .addBoolean("optimize_dynamic_graph",
                                "config.tritium.performance.lightingOptimizations_optimizeDynamicGraph",
                                (c, v) -> TritiumConfigBase.Performance.LightingOptimizations.optimizeDynamicGraph = v,
                                c -> TritiumConfigBase.Performance.LightingOptimizations.optimizeDynamicGraph)
                        .addBoolean("bamboo_light",
                                "config.tritium.performance.lightingOptimizations_bambooLight",
                                (c, v) -> TritiumConfigBase.Performance.LightingOptimizations.bambooLight = v,
                                c -> TritiumConfigBase.Performance.LightingOptimizations.bambooLight))
                .build();
    }

    public static TritiumEmbPage createRenderingPage() {
        return new TritiumPageBuilder("rendering", Component.translatable("config.tritium.category.rendering"))
                .addGroup("group_chest_rendering_opt", Component.translatable("config.tritium.rendering.cro"), builder -> builder
                        .addBoolean("chest_rendering_opt",
                                "config.tritium.rendering.cro_chest_rendering_optimization",
                                (c, v) -> TritiumConfigBase.Rendering.CRO.chest_rendering_optimization = v,
                                c -> TritiumConfigBase.Rendering.CRO.chest_rendering_optimization))
                .addGroup("group_fast_blit", Component.translatable("config.tritium.rendering.fastBlit"), builder -> builder
                        .addBoolean("fast_blit",
                                "config.tritium.rendering.fastBlit_fastBlit",
                                (c, v) -> TritiumConfigBase.Rendering.FastBlit.fastBlit = v,
                                c -> TritiumConfigBase.Rendering.FastBlit.fastBlit))
                .addGroup("reflex", Component.translatable("config.tritium.rendering.reflex"), builder -> builder
                        .addBoolean("enable_reflex",
                                "config.tritium.rendering.reflex_enableReflex",
                                (c, v) -> TritiumConfigBase.Rendering.Reflex.enableReflex = v,
                                c -> TritiumConfigBase.Rendering.Reflex.enableReflex)
                        .addBoolean("reflex_debug",
                                "config.tritium.rendering.reflex_reflexDebug",
                                (c, v) -> TritiumConfigBase.Rendering.Reflex.reflexDebug = v,
                                c -> TritiumConfigBase.Rendering.Reflex.reflexDebug)
                        .addInteger("reflex_offset_ns",
                                "config.tritium.rendering.reflex_reflexOffsetNs",
                                -100000, 100000, 1000,
                                ControlValueFormatter.number(),
                                (c, v) -> TritiumConfigBase.Rendering.Reflex.reflexOffsetNs = v,
                                c -> TritiumConfigBase.Rendering.Reflex.reflexOffsetNs)
                        .addInteger("max_fps",
                                "config.tritium.rendering.reflex_MAX_FPS",
                                0, 1000, 10,
                                v -> v > 0 ? Component.literal(v + " FPS") : CommonComponents.OPTION_OFF,
                                (c, v) -> TritiumConfigBase.Rendering.Reflex.MAX_FPS = v,
                                c -> TritiumConfigBase.Rendering.Reflex.MAX_FPS))
                .addGroup("entity_culling", Component.translatable("config.tritium.rendering.entityCulling"), builder -> builder
                        .addBoolean("enable_culling",
                                "config.tritium.rendering.entityCulling_enableCulling",
                                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableCulling = v,
                                c -> TritiumConfigBase.Rendering.EntityCulling.enableCulling)
                        .addBoolean("enable_block_entity_culling",
                                "config.tritium.rendering.entityCulling_enableBlockEntityCulling",
                                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableBlockEntityCulling = v,
                                c -> TritiumConfigBase.Rendering.EntityCulling.enableBlockEntityCulling)
                        .addBoolean("enable_tick_stopping",
                                "config.tritium.rendering.entityCulling_enableTickStopping",
                                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableTickStopping = v,
                                c -> TritiumConfigBase.Rendering.EntityCulling.enableTickStopping))
                .addGroup("leaf_culling", Component.translatable("config.tritium.rendering.leafCulling"), builder -> builder
                        .addBoolean("enable_leaf_culling",
                                "config.tritium.rendering.leafCulling_enableLeafCulling",
                                (c, v) -> TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling = v,
                                c -> TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling)
                        .addBoolean("hide_inner_leaves",
                                "config.tritium.rendering.leafCulling_hideInnerLeaves",
                                (c, v) -> TritiumConfigBase.Rendering.LeafCulling.hideInnerLeaves = v,
                                c -> TritiumConfigBase.Rendering.LeafCulling.hideInnerLeaves)
                        .addBoolean("enable_face_occlusion_culling",
                                "config.tritium.rendering.leafCulling_enableFaceOcclusionCulling",
                                (c, v) -> TritiumConfigBase.Rendering.LeafCulling.enableFaceOcclusionCulling = v,
                                c -> TritiumConfigBase.Rendering.LeafCulling.enableFaceOcclusionCulling))
                .build();
    }

    public static TritiumEmbPage createClientOptimizationsPage() {
        return new TritiumPageBuilder("client_optimizations", Component.translatable("config.tritium.category.clientOptimizations"))
                .addGroup("group_fast_language", Component.translatable("config.tritium.clientOptimizations.FL"), builder -> builder
                        .addBoolean("fast_language",
                                "config.tritium.clientOptimizations.FL_fastLanguageSwitch",
                                (c, v) -> TritiumConfigBase.ClientOptimizations.FL.fastLanguageSwitch = v,
                                c -> TritiumConfigBase.ClientOptimizations.FL.fastLanguageSwitch))
                .addGroup("dynamic_fps", Component.translatable("config.tritium.clientOptimizations.dynamicFPS"), builder -> builder
                        .addBoolean("enable_dynamic_fps",
                                "config.tritium.clientOptimizations.dynamicFPS_enable",
                                (c, v) -> TritiumConfigBase.ClientOptimizations.DynamicFPS.enable = v,
                                c -> TritiumConfigBase.ClientOptimizations.DynamicFPS.enable)
                        .addInteger("minimized_fps",
                                "config.tritium.clientOptimizations.dynamicFPS_minimizedFPS",
                                1, 60, 1,
                                v -> v > 1 ? Component.literal(v + " FPS") : Component.literal("1 FPS"),
                                (c, v) -> TritiumConfigBase.ClientOptimizations.DynamicFPS.minimizedFPS = v,
                                c -> TritiumConfigBase.ClientOptimizations.DynamicFPS.minimizedFPS))
                .addGroup("fps_display", Component.translatable("config.tritium.fpsdisplan.fpsDisplay"), builder -> builder
                        .addBoolean("fps_display_enabled",
                                "config.tritium.fpsdisplan.fpsDisplay_enabled",
                                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.enabled = v,
                                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.enabled)
                        .addEnum("fps_display_position",
                                "config.tritium.fpsdisplan.fpsDisplay_position",
                                TritiumConfigBase.FPSDisplan.Position.class,
                                new Component[]{
                                        Component.translatable("config.tritium.fpsDisplay.position.topLeft"),
                                        Component.translatable("config.tritium.fpsDisplay.position.topRight"),
                                        Component.translatable("config.tritium.fpsDisplay.position.bottomLeft"),
                                        Component.translatable("config.tritium.fpsDisplay.position.bottomRight"),
                                        Component.translatable("config.tritium.fpsDisplay.position.center")
                                },
                                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.position = v,
                                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.position)
                        .addEnum("fps_display_mode",
                                "config.tritium.fpsdisplan.fpsDisplay_displayMode",
                                TritiumConfigBase.FPSDisplan.DisplayMode.class,
                                new Component[]{
                                        Component.translatable("config.tritium.fpsDisplay.mode.avgOnly"),
                                        Component.translatable("config.tritium.fpsDisplay.mode.currentOnly"),
                                        Component.translatable("config.tritium.fpsDisplay.mode.all"),
                                        Component.translatable("config.tritium.fpsDisplay.mode.maxOnly"),
                                        Component.translatable("config.tritium.fpsDisplay.mode.minOnly")
                                },
                                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.displayMode = v,
                                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.displayMode)
                        .addBoolean("shadow",
                                "config.tritium.fpsdisplan.fpsDisplay_shadow",
                                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.shadow = v,
                                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.shadow)
                        .addFloat("background_opacity",
                                "config.tritium.fpsdisplan.fpsDisplay_backgroundOpacity",
                                0.0f, 1.0f, 0.05f,
                                ControlValueFormatter.percentage(),
                                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.backgroundOpacity = v,
                                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.backgroundOpacity)
                        .addBoolean("show_unit",
                                "config.tritium.fpsdisplan.fpsDisplay_showUnit",
                                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.showUnit = v,
                                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.showUnit))
                .addGroup("group_particle_limit", Component.translatable("config.tritium.category.particleLimit"), builder -> builder
                        .addBoolean("enable_particle_limit",
                                "config.tritium.particleLimit.enableParticleLimit",
                                (c, v) -> TritiumConfigBase.ParticleLimit.enableParticleLimit = v,
                                c -> TritiumConfigBase.ParticleLimit.enableParticleLimit)
                        .addInteger("max_particles",
                                "config.tritium.particleLimit.maxParticles",
                                100, 50000, 100,
                                v -> Component.literal(v + " particles"),
                                (c, v) -> TritiumConfigBase.ParticleLimit.maxParticles = v,
                                c -> TritiumConfigBase.ParticleLimit.maxParticles))
                .build();
    }

    public static TritiumEmbPage createEntitiesPage() {
        return new TritiumPageBuilder("entities", Component.translatable("config.tritium.category.entities"))
                .addGroup("entity_opt", Component.translatable("config.tritium.entities.entityOpt"), builder -> builder
                        .addBoolean("optimize_entities",
                                "config.tritium.entities.entityOpt_optimizeEntities",
                                (c, v) -> TritiumConfigBase.Entities.EntityOpt.optimizeEntities = v,
                                c -> TritiumConfigBase.Entities.EntityOpt.optimizeEntities)
                        .addBoolean("tick_raiders_in_raid",
                                "config.tritium.entities.entityOpt_tickRaidersInRaid",
                                (c, v) -> TritiumConfigBase.Entities.EntityOpt.tickRaidersInRaid = v,
                                c -> TritiumConfigBase.Entities.EntityOpt.tickRaidersInRaid)
                        .addBoolean("ite",
                                "config.tritium.entities.entityOpt_ite",
                                (c, v) -> TritiumConfigBase.Entities.EntityOpt.ite = v,
                                c -> TritiumConfigBase.Entities.EntityOpt.ite)
                        .addInteger("horizontal_range",
                                "config.tritium.entities.entityOpt_horizontalRange",
                                1, 256, 1,
                                v -> Component.literal(v + " blocks"),
                                (c, v) -> TritiumConfigBase.Entities.EntityOpt.horizontalRange = v,
                                c -> TritiumConfigBase.Entities.EntityOpt.horizontalRange)
                        .addInteger("vertical_range",
                                "config.tritium.entities.entityOpt_verticalRange",
                                1, 256, 1,
                                v -> Component.literal(v + " blocks"),
                                (c, v) -> TritiumConfigBase.Entities.EntityOpt.verticalRange = v,
                                c -> TritiumConfigBase.Entities.EntityOpt.verticalRange))
                .addGroup("entity_stacking", Component.translatable("config.tritium.entities.entityStacking"), builder -> builder
                        .addBoolean("enable",
                                "config.tritium.entities.entityStacking_enable",
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.enable = v,
                                c -> TritiumConfigBase.Entities.EntityStacking.enable)
                        .addBoolean("lock_maxed_stacks",
                                "config.tritium.entities.entityStacking_lockMaxedStacks",
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.lockMaxedStacks = v,
                                c -> TritiumConfigBase.Entities.EntityStacking.lockMaxedStacks)
                        .addBoolean("show_stack_count",
                                "config.tritium.entities.entityStacking_showStackCount",
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.showStackCount = v,
                                c -> TritiumConfigBase.Entities.EntityStacking.showStackCount)
                        .addInteger("max_stack_size",
                                "config.tritium.entities.entityStacking_maxStackSize",
                                0, 100, 1,
                                v -> v > 0 ? Component.literal(v + " entities") : CommonComponents.OPTION_OFF,
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.maxStackSize = v,
                                c -> TritiumConfigBase.Entities.EntityStacking.maxStackSize)
                        .addInteger("merge_cooldown",
                                "config.tritium.entities.entityStacking_mergeCooldown",
                                0, 100, 1,
                                v -> Component.literal(v + " ticks"),
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.mergeCooldown = v,
                                c -> TritiumConfigBase.Entities.EntityStacking.mergeCooldown)
                        .addFloat("merge_distance",
                                "config.tritium.entities.entityStacking_mergeDistance",
                                0.1f, 10.0f, 0.1f,
                                v -> {
                                    float floatValue = v / 100f;
                                    return Component.literal(String.format("%.1f blocks", floatValue));
                                },
                                (c, v) -> TritiumConfigBase.Entities.EntityStacking.mergeDistance = v.doubleValue(),
                                c -> (float) TritiumConfigBase.Entities.EntityStacking.mergeDistance))
                .build();
    }

    public static TritiumEmbPage createFixesPage() {
        return new TritiumPageBuilder("fixes", Component.translatable("config.tritium.category.fixes"))
                .addGroup("group_button_fix", Component.translatable("config.tritium.fixes.buttonFix"), builder -> builder
                        .addBoolean("button_fix",
                                "config.tritium.fixes.buttonFix_buttonFix",
                                (c, v) -> TritiumConfigBase.Fixes.ButtonFix.buttonFix = v,
                                c -> TritiumConfigBase.Fixes.ButtonFix.buttonFix))
                .addGroup("group_no_glog", Component.translatable("config.tritium.fixes.noGLog"), builder -> builder
                        .addBoolean("no_glog",
                                "config.tritium.fixes.noGLog_noGLog",
                                (c, v) -> TritiumConfigBase.Fixes.NoGLog.noGLog = v,
                                c -> TritiumConfigBase.Fixes.NoGLog.noGLog))
                .addGroup("memory_leak_fix", Component.translatable("config.tritium.fixes.memoryLeakFix"), builder -> builder
                        .addBoolean("ae2wt_lib_creative_tab_leak_fix",
                                "config.tritium.fixes.memoryLeakFix_AE2WTLibCreativeTabLeakFix",
                                (c, v) -> TritiumConfigBase.Fixes.MemoryLeakFix.AE2WTLibCreativeTabLeakFix = v,
                                c -> TritiumConfigBase.Fixes.MemoryLeakFix.AE2WTLibCreativeTabLeakFix))
                .addGroup("bee_fixes", Component.translatable("config.tritium.fixes.beeFixes"), builder -> builder
                        .addBoolean("enable_bee_fixes",
                                "config.tritium.fixes.beeFixes_enableBeeFixes",
                                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes = v,
                                c -> TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes)
                        .addBoolean("fix_weather_in_nether",
                                "config.tritium.fixes.beeFixes_fixWeatherInNether",
                                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.fixWeatherInNether = v,
                                c -> TritiumConfigBase.Fixes.BeeFixes.fixWeatherInNether)
                        .addBoolean("fix_bee_random_pos",
                                "config.tritium.fixes.beeFixes_fixBeeRandomPos",
                                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.fixBeeRandomPos = v,
                                c -> TritiumConfigBase.Fixes.BeeFixes.fixBeeRandomPos)
                        .addBoolean("fix_bee_gravity",
                                "config.tritium.fixes.beeFixes_fixBeeGravity",
                                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.fixBeeGravity = v,
                                c -> TritiumConfigBase.Fixes.BeeFixes.fixBeeGravity)
                        .addBoolean("fix_bee_turtle_egg",
                                "config.tritium.fixes.beeFixes_fixBeeTurtleEgg",
                                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.fixBeeTurtleEgg = v,
                                c -> TritiumConfigBase.Fixes.BeeFixes.fixBeeTurtleEgg))
                .build();
    }

    public static TritiumEmbPage createServerPerformancePage() {
        return new TritiumPageBuilder("server_performance", Component.translatable("config.tritium.category.serverPerformance"))
                .addGroup("group_noise_sampling_cache", Component.translatable("config.tritium.serverPerformance.noiseSamplingCache"), builder -> builder
                        .addBoolean("noise_sampling_cache",
                                "config.tritium.serverPerformance.noiseSamplingCache_noiseSamplingCache",
                                (c, v) -> TritiumConfigBase.ServerPerformance.NoiseSamplingCache.noiseSamplingCache = v,
                                c -> TritiumConfigBase.ServerPerformance.NoiseSamplingCache.noiseSamplingCache))
                .addGroup("jigsaw_optimizations", Component.translatable("config.tritium.serverPerformance.jigsawOptimizations"), builder -> builder
                        .addBoolean("enable_jigsaw_optimizations",
                                "config.tritium.serverPerformance.jigsawOptimizations_enableJigsawOptimizations",
                                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations = v,
                                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations)
                        .addBoolean("enable_octree_collision_detection",
                                "config.tritium.serverPerformance.jigsawOptimizations_enableOctreeCollisionDetection",
                                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableOctreeCollisionDetection = v,
                                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableOctreeCollisionDetection)
                        .addBoolean("enable_fast_weighted_sampling",
                                "config.tritium.serverPerformance.jigsawOptimizations_enableFastWeightedSampling",
                                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableFastWeightedSampling = v,
                                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableFastWeightedSampling)
                        .addBoolean("enable_structure_block_filtering",
                                "config.tritium.serverPerformance.jigsawOptimizations_enableStructureBlockFiltering",
                                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableStructureBlockFiltering = v,
                                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableStructureBlockFiltering)
                        .addBoolean("enable_jigsaw_generation_check",
                                "config.tritium.serverPerformance.jigsawOptimizations_enableJigsawGenerationCheck",
                                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawGenerationCheck = v,
                                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawGenerationCheck))
                .build();
    }

    public static TritiumEmbPage createTechOptimizationsPage() {
        return new TritiumPageBuilder("tech_optimizations", Component.translatable("config.tritium.category.techOptimizations"))
                .addGroup("create_optimizations", Component.translatable("config.tritium.techOptimizations.createOptimizations"), builder -> builder
                        .addBoolean("enable_rail_offloading",
                                "config.tritium.techOptimizations.createOptimizations_enableRailOffloading",
                                (c, v) -> TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading = v,
                                c -> TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading))
                .build();
    }

    public static TritiumEmbPage createNetworkPage() {
        return new TritiumPageBuilder("network", Component.translatable("config.tritium.category.network"))
                .addGroup("network", Component.translatable("config.tritium.category.network"), builder -> {
                })
                .build();
    }
}
