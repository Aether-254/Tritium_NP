package org.craftamethyst.tritium.integration.sodium;

import com.google.common.collect.ImmutableList;
import net.caffeinemc.mods.sodium.client.gui.options.*;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.TickBoxControl;
import net.minecraft.network.chat.Component;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.ArrayList;
import java.util.List;

public class TritiumSodiumOptions {
    private final TritiumOptionsStorage storage;

    public TritiumSodiumOptions(TritiumConfigBase config) {
        this.storage = new TritiumOptionsStorage(config);
    }

    public List<OptionPage> createOptionPages() {
        List<OptionPage> pages = new ArrayList<>();

        pages.add(createPerformancePage());
        pages.add(createRenderingPage());
        pages.add(createClientOptimizationsPage());
        pages.add(createEntitiesPage());
        pages.add(createTechOptimizationsPage());
        pages.add(createFixesPage());
        pages.add(createServerPerformancePage());

        return pages;
    }

    private OptionPage createPerformancePage() {
        List<OptionGroup> groups = new ArrayList<>();

        List<TritiumOptionDefinition<?>> lightingOptimizations = new ArrayList<>();
        lightingOptimizations.add(new TritiumOptionDefinition<>(
                "performance.lightingOptimizations_enableLightingOptimizations",
                "config.tritium.performance.lightingOptimizations_enableLightingOptimizations",
                c -> TritiumConfigBase.Performance.LightingOptimizations.enableLightingOptimizations,
                (c, v) -> TritiumConfigBase.Performance.LightingOptimizations.enableLightingOptimizations = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        lightingOptimizations.add(new TritiumOptionDefinition<>(
                "performance.lightingOptimizations_optimizeDynamicGraph",
                "config.tritium.performance.lightingOptimizations_optimizeDynamicGraph",
                c -> TritiumConfigBase.Performance.LightingOptimizations.optimizeDynamicGraph,
                (c, v) -> TritiumConfigBase.Performance.LightingOptimizations.optimizeDynamicGraph = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        lightingOptimizations.add(new TritiumOptionDefinition<>(
                "performance.lightingOptimizations_bambooLight",
                "config.tritium.performance.lightingOptimizations_bambooLight",
                c -> TritiumConfigBase.Performance.LightingOptimizations.bambooLight,
                (c, v) -> TritiumConfigBase.Performance.LightingOptimizations.bambooLight = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(lightingOptimizations));

        List<TritiumOptionDefinition<?>> fastFurnace = new ArrayList<>();
        fastFurnace.add(new TritiumOptionDefinition<>(
                "performance.fastFurnace_fastFurnace",
                "config.tritium.performance.fastFurnace_fastFurnace",
                c -> TritiumConfigBase.Performance.FastFurnace.fastFurnace,
                (c, v) -> TritiumConfigBase.Performance.FastFurnace.fastFurnace = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(fastFurnace));

        List<TritiumOptionDefinition<?>> blockStateCache = new ArrayList<>();
        blockStateCache.add(new TritiumOptionDefinition<>(
                "performance.blockStateCache_blockStatePairKeyCache",
                "config.tritium.performance.blockStateCache_blockStatePairKeyCache",
                c -> TritiumConfigBase.Performance.BlockStateCache.blockStatePairKeyCache,
                (c, v) -> TritiumConfigBase.Performance.BlockStateCache.blockStatePairKeyCache = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(blockStateCache));

        return new OptionPage(
                Component.translatable("config.tritium.category.performance"),
                ImmutableList.copyOf(groups)
        );
    }

    private OptionPage createRenderingPage() {
        List<OptionGroup> groups = new ArrayList<>();

        List<TritiumOptionDefinition<?>> cro = new ArrayList<>();
        cro.add(new TritiumOptionDefinition<>(
                "rendering.cro_chest_rendering_optimization",
                "config.tritium.rendering.cro_chest_rendering_optimization",
                c -> TritiumConfigBase.Rendering.CRO.chest_rendering_optimization,
                (c, v) -> TritiumConfigBase.Rendering.CRO.chest_rendering_optimization = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(cro));

        List<TritiumOptionDefinition<?>> fastBlit = new ArrayList<>();
        fastBlit.add(new TritiumOptionDefinition<>(
                "rendering.fastBlit_fastBlit",
                "config.tritium.rendering.fastBlit_fastBlit",
                c -> TritiumConfigBase.Rendering.FastBlit.fastBlit,
                (c, v) -> TritiumConfigBase.Rendering.FastBlit.fastBlit = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(fastBlit));

        List<TritiumOptionDefinition<?>> gpuPlus = new ArrayList<>();
        gpuPlus.add(new TritiumOptionDefinition<>(
                "rendering.GpuPlus_gpuPlus",
                "config.tritium.rendering.GpuPlus_gpuPlus",
                c -> TritiumConfigBase.Rendering.GpuPlus.gpuPlus,
                (c, v) -> TritiumConfigBase.Rendering.GpuPlus.gpuPlus = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        gpuPlus.add(new TritiumOptionDefinition<>(
                "rendering.GpuPlus_gpuPlusVbo",
                "config.tritium.rendering.GpuPlus_gpuPlusVbo",
                c -> TritiumConfigBase.Rendering.GpuPlus.gpuPlusVbo,
                (c, v) -> TritiumConfigBase.Rendering.GpuPlus.gpuPlusVbo = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        gpuPlus.add(new TritiumOptionDefinition<>(
                "rendering.GpuPlus_gpuPlusIndex",
                "config.tritium.rendering.GpuPlus_gpuPlusIndex",
                c -> TritiumConfigBase.Rendering.GpuPlus.gpuPlusIndex,
                (c, v) -> TritiumConfigBase.Rendering.GpuPlus.gpuPlusIndex = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(gpuPlus));

        List<TritiumOptionDefinition<?>> reflex = new ArrayList<>();
        reflex.add(new TritiumOptionDefinition<>(
                "rendering.reflex_enableReflex",
                "config.tritium.rendering.reflex_enableReflex",
                c -> TritiumConfigBase.Rendering.Reflex.enableReflex,
                (c, v) -> TritiumConfigBase.Rendering.Reflex.enableReflex = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        reflex.add(new TritiumOptionDefinition<>(
                "rendering.reflex_reflexDebug",
                "config.tritium.rendering.reflex_reflexDebug",
                c -> TritiumConfigBase.Rendering.Reflex.reflexDebug,
                (c, v) -> TritiumConfigBase.Rendering.Reflex.reflexDebug = v,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        reflex.add(new TritiumOptionDefinition<>(
                "rendering.reflex_reflexOffsetNs",
                "config.tritium.rendering.reflex_reflexOffsetNs",
                c -> TritiumConfigBase.Rendering.Reflex.reflexOffsetNs,
                (c, v) -> TritiumConfigBase.Rendering.Reflex.reflexOffsetNs = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                -100000, 100000, 1000
        ));
        reflex.add(new TritiumOptionDefinition<>(
                "rendering.reflex_MAX_FPS",
                "config.tritium.rendering.reflex_MAX_FPS",
                c -> TritiumConfigBase.Rendering.Reflex.MAX_FPS,
                (c, v) -> TritiumConfigBase.Rendering.Reflex.MAX_FPS = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                0, 1000, 10
        ));
        groups.add(createGroup(reflex));

        List<TritiumOptionDefinition<?>> entityCulling = new ArrayList<>();
        entityCulling.add(new TritiumOptionDefinition<>(
                "rendering.entityCulling_enableCulling",
                "config.tritium.rendering.entityCulling_enableCulling",
                c -> TritiumConfigBase.Rendering.EntityCulling.enableCulling,
                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableCulling = v,
                OptionImpact.HIGH,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityCulling.add(new TritiumOptionDefinition<>(
                "rendering.entityCulling_enableBlockEntityCulling",
                "config.tritium.rendering.entityCulling_enableBlockEntityCulling",
                c -> TritiumConfigBase.Rendering.EntityCulling.enableBlockEntityCulling,
                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableBlockEntityCulling = v,
                OptionImpact.HIGH,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityCulling.add(new TritiumOptionDefinition<>(
                "rendering.entityCulling_enableTickStopping",
                "config.tritium.rendering.entityCulling_enableTickStopping",
                c -> TritiumConfigBase.Rendering.EntityCulling.enableTickStopping,
                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableTickStopping = v,
                OptionImpact.HIGH,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityCulling.add(new TritiumOptionDefinition<>(
                "rendering.entityCulling_enableNameTagCulling",
                "config.tritium.rendering.entityCulling_enableNameTagCulling",
                c -> TritiumConfigBase.Rendering.EntityCulling.enableNameTagCulling,
                (c, v) -> TritiumConfigBase.Rendering.EntityCulling.enableNameTagCulling = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(entityCulling));

        List<TritiumOptionDefinition<?>> leafCulling = new ArrayList<>();
        leafCulling.add(new TritiumOptionDefinition<>(
                "rendering.leafCulling_enableLeafCulling",
                "config.tritium.rendering.leafCulling_enableLeafCulling",
                c -> TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling,
                (c, v) -> TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        leafCulling.add(new TritiumOptionDefinition<>(
                "rendering.leafCulling_hideInnerLeaves",
                "config.tritium.rendering.leafCulling_hideInnerLeaves",
                c -> TritiumConfigBase.Rendering.LeafCulling.hideInnerLeaves,
                (c, v) -> TritiumConfigBase.Rendering.LeafCulling.hideInnerLeaves = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        leafCulling.add(new TritiumOptionDefinition<>(
                "rendering.leafCulling_enableFaceOcclusionCulling",
                "config.tritium.rendering.leafCulling_enableFaceOcclusionCulling",
                c -> TritiumConfigBase.Rendering.LeafCulling.enableFaceOcclusionCulling,
                (c, v) -> TritiumConfigBase.Rendering.LeafCulling.enableFaceOcclusionCulling = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(leafCulling));

        return new OptionPage(
                Component.translatable("config.tritium.category.rendering"),
                ImmutableList.copyOf(groups)
        );
    }

    private OptionPage createClientOptimizationsPage() {
        List<OptionGroup> groups = new ArrayList<>();

        List<TritiumOptionDefinition<?>> fl = new ArrayList<>();
        fl.add(new TritiumOptionDefinition<>(
                "clientOptimizations.FL_fastLanguageSwitch",
                "config.tritium.clientOptimizations.FL_fastLanguageSwitch",
                c -> TritiumConfigBase.ClientOptimizations.FL.fastLanguageSwitch,
                (c, v) -> TritiumConfigBase.ClientOptimizations.FL.fastLanguageSwitch = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(fl));

        List<TritiumOptionDefinition<?>> fastResourcePack = new ArrayList<>();
        fastResourcePack.add(new TritiumOptionDefinition<>(
                "clientOptimizations.FastResourcePack_resourcePackCache",
                "config.tritium.clientOptimizations.FastResourcePack_resourcePackCache",
                c -> TritiumConfigBase.ClientOptimizations.FastResourcePack.resourcePackCache,
                (c, v) -> TritiumConfigBase.ClientOptimizations.FastResourcePack.resourcePackCache = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(fastResourcePack));

        List<TritiumOptionDefinition<?>> dynamicFPS = new ArrayList<>();
        dynamicFPS.add(new TritiumOptionDefinition<>(
                "clientOptimizations.dynamicFPS_enable",
                "config.tritium.clientOptimizations.dynamicFPS_enable",
                c -> TritiumConfigBase.ClientOptimizations.DynamicFPS.enable,
                (c, v) -> TritiumConfigBase.ClientOptimizations.DynamicFPS.enable = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        dynamicFPS.add(new TritiumOptionDefinition<>(
                "clientOptimizations.dynamicFPS_minimizedFPS",
                "config.tritium.clientOptimizations.dynamicFPS_minimizedFPS",
                c -> TritiumConfigBase.ClientOptimizations.DynamicFPS.minimizedFPS,
                (c, v) -> TritiumConfigBase.ClientOptimizations.DynamicFPS.minimizedFPS = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                1, 60, 1
        ));
        groups.add(createGroup(dynamicFPS));

        List<TritiumOptionDefinition<?>> fpsDisplay = new ArrayList<>();
        fpsDisplay.add(new TritiumOptionDefinition<>(
                "fpsdisplan.fpsDisplay_enabled",
                "config.tritium.fpsdisplan.fpsDisplay_enabled",
                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.enabled,
                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.enabled = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        fpsDisplay.add(new TritiumOptionDefinition<>(
                "fpsdisplan.fpsDisplay_position",
                "config.tritium.fpsdisplan.fpsDisplay_position",
                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.position,
                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.position = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                v -> switch(v) {
                    case 0 -> Component.translatable("config.tritium.fpsDisplay.position.topLeft");
                    case 1 -> Component.translatable("config.tritium.fpsDisplay.position.topRight");
                    case 2 -> Component.translatable("config.tritium.fpsDisplay.position.bottomLeft");
                    case 3 -> Component.translatable("config.tritium.fpsDisplay.position.bottomRight");
                    case 4 -> Component.translatable("config.tritium.fpsDisplay.position.center");
                    default -> Component.literal(String.valueOf(v));
                },
                0, 4, 1
        ));
        fpsDisplay.add(new TritiumOptionDefinition<>(
                "fpsdisplan.fpsDisplay_displayMode",
                "config.tritium.fpsdisplan.fpsDisplay_displayMode",
                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.displayMode,
                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.displayMode = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                v -> switch(v) {
                    case 0 -> Component.translatable("config.tritium.fpsDisplay.mode.avgOnly");
                    case 1 -> Component.translatable("config.tritium.fpsDisplay.mode.currentOnly");
                    case 2 -> Component.translatable("config.tritium.fpsDisplay.mode.all");
                    case 3 -> Component.translatable("config.tritium.fpsDisplay.mode.maxOnly");
                    case 4 -> Component.translatable("config.tritium.fpsDisplay.mode.minOnly");
                    default -> Component.literal(String.valueOf(v));
                },
                0, 4, 1
        ));
        fpsDisplay.add(new TritiumOptionDefinition<>(
                "fpsdisplan.fpsDisplay_backgroundOpacity",
                "config.tritium.fpsdisplan.fpsDisplay_backgroundOpacity",
                c -> (int) (TritiumConfigBase.FPSDisplan.FPSDisplay.backgroundOpacity * 100),
                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.backgroundOpacity = v / 100.0f,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                ControlValueFormatter.percentage(),
                0, 100, 5
        ));
        fpsDisplay.add(new TritiumOptionDefinition<>(
                "fpsdisplan.fpsDisplay_decimalPlaces",
                "config.tritium.fpsdisplan.fpsDisplay_decimalPlaces",
                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.decimalPlaces,
                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.decimalPlaces = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                0, 2, 1
        ));
        fpsDisplay.add(new TritiumOptionDefinition<>(
                "fpsdisplan.fpsDisplay_shadow",
                "config.tritium.fpsdisplan.fpsDisplay_shadow",
                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.shadow,
                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.shadow = v,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        fpsDisplay.add(new TritiumOptionDefinition<>(
                "fpsdisplan.fpsDisplay_showUnit",
                "config.tritium.fpsdisplan.fpsDisplay_showUnit",
                c -> TritiumConfigBase.FPSDisplan.FPSDisplay.showUnit,
                (c, v) -> TritiumConfigBase.FPSDisplan.FPSDisplay.showUnit = v,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(fpsDisplay));

        List<TritiumOptionDefinition<?>> particleLimit = new ArrayList<>();
        particleLimit.add(new TritiumOptionDefinition<>(
                "particleLimit.enableParticleLimit",
                "config.tritium.particleLimit.enableParticleLimit",
                c -> TritiumConfigBase.ParticleLimit.enableParticleLimit,
                (c, v) -> TritiumConfigBase.ParticleLimit.enableParticleLimit = v,
                OptionImpact.HIGH,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        particleLimit.add(new TritiumOptionDefinition<>(
                "particleLimit.maxParticles",
                "config.tritium.particleLimit.maxParticles",
                c -> TritiumConfigBase.ParticleLimit.maxParticles,
                (c, v) -> TritiumConfigBase.ParticleLimit.maxParticles = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                100, 50000, 100
        ));
        groups.add(createGroup(particleLimit));

        return new OptionPage(
                Component.translatable("config.tritium.category.clientOptimizations"),
                ImmutableList.copyOf(groups)
        );
    }

    private OptionPage createEntitiesPage() {
        List<OptionGroup> groups = new ArrayList<>();

        List<TritiumOptionDefinition<?>> entityOpt = new ArrayList<>();
        entityOpt.add(new TritiumOptionDefinition<>(
                "entities.entityOpt_optimizeEntities",
                "config.tritium.entities.entityOpt_optimizeEntities",
                c -> TritiumConfigBase.Entities.EntityOpt.optimizeEntities,
                (c, v) -> TritiumConfigBase.Entities.EntityOpt.optimizeEntities = v,
                OptionImpact.HIGH,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityOpt.add(new TritiumOptionDefinition<>(
                "entities.entityOpt_tickRaidersInRaid",
                "config.tritium.entities.entityOpt_tickRaidersInRaid",
                c -> TritiumConfigBase.Entities.EntityOpt.tickRaidersInRaid,
                (c, v) -> TritiumConfigBase.Entities.EntityOpt.tickRaidersInRaid = v,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityOpt.add(new TritiumOptionDefinition<>(
                "entities.entityOpt_ite",
                "config.tritium.entities.entityOpt_ite",
                c -> TritiumConfigBase.Entities.EntityOpt.ite,
                (c, v) -> TritiumConfigBase.Entities.EntityOpt.ite = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityOpt.add(new TritiumOptionDefinition<>(
                "entities.entityOpt_horizontalRange",
                "config.tritium.entities.entityOpt_horizontalRange",
                c -> TritiumConfigBase.Entities.EntityOpt.horizontalRange,
                (c, v) -> TritiumConfigBase.Entities.EntityOpt.horizontalRange = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                1, 256, 1
        ));
        entityOpt.add(new TritiumOptionDefinition<>(
                "entities.entityOpt_verticalRange",
                "config.tritium.entities.entityOpt_verticalRange",
                c -> TritiumConfigBase.Entities.EntityOpt.verticalRange,
                (c, v) -> TritiumConfigBase.Entities.EntityOpt.verticalRange = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                1, 256, 1
        ));
        groups.add(createGroup(entityOpt));

        List<TritiumOptionDefinition<?>> entityStacking = new ArrayList<>();
        entityStacking.add(new TritiumOptionDefinition<>(
                "entities.entityStacking_enable",
                "config.tritium.entities.entityStacking_enable",
                c -> TritiumConfigBase.Entities.EntityStacking.enable,
                (c, v) -> TritiumConfigBase.Entities.EntityStacking.enable = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityStacking.add(new TritiumOptionDefinition<>(
                "entities.entityStacking_lockMaxedStacks",
                "config.tritium.entities.entityStacking_lockMaxedStacks",
                c -> TritiumConfigBase.Entities.EntityStacking.lockMaxedStacks,
                (c, v) -> TritiumConfigBase.Entities.EntityStacking.lockMaxedStacks = v,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityStacking.add(new TritiumOptionDefinition<>(
                "entities.entityStacking_showStackCount",
                "config.tritium.entities.entityStacking_showStackCount",
                c -> TritiumConfigBase.Entities.EntityStacking.showStackCount,
                (c, v) -> TritiumConfigBase.Entities.EntityStacking.showStackCount = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        entityStacking.add(new TritiumOptionDefinition<>(
                "entities.entityStacking_maxStackSize",
                "config.tritium.entities.entityStacking_maxStackSize",
                c -> TritiumConfigBase.Entities.EntityStacking.maxStackSize,
                (c, v) -> TritiumConfigBase.Entities.EntityStacking.maxStackSize = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                0, 100, 1
        ));
        entityStacking.add(new TritiumOptionDefinition<>(
                "entities.entityStacking_mergeCooldown",
                "config.tritium.entities.entityStacking_mergeCooldown",
                c -> TritiumConfigBase.Entities.EntityStacking.mergeCooldown,
                (c, v) -> TritiumConfigBase.Entities.EntityStacking.mergeCooldown = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                0, 100, 1
        ));
        entityStacking.add(new TritiumOptionDefinition<>(
                "entities.entityStacking_mergeDistance",
                "config.tritium.entities.entityStacking_mergeDistance",
                c -> TritiumConfigBase.Entities.EntityStacking.mergeDistance,
                (c, v) -> TritiumConfigBase.Entities.EntityStacking.mergeDistance = v,
                TritiumOptionDefinition.ControlType.DOUBLE_SLIDER,
                0.1, 10.0, 0.1
        ));
        entityStacking.add(new TritiumOptionDefinition<>(
                "entities.entityStacking_listMode",
                "config.tritium.entities.entityStacking_listMode",
                c -> TritiumConfigBase.Entities.EntityStacking.listMode,
                (c, v) -> TritiumConfigBase.Entities.EntityStacking.listMode = v,
                TritiumOptionDefinition.ControlType.INTEGER_SLIDER,
                0, 2, 1
        ));
        groups.add(createGroup(entityStacking));

        return new OptionPage(
                Component.translatable("config.tritium.category.entities"),
                ImmutableList.copyOf(groups)
        );
    }

    private OptionPage createTechOptimizationsPage() {
        List<OptionGroup> groups = new ArrayList<>();

        List<TritiumOptionDefinition<?>> createOptimizations = new ArrayList<>();
        createOptimizations.add(new TritiumOptionDefinition<>(
                "techOptimizations.createOptimizations_enableRailOffloading",
                "config.tritium.techOptimizations.createOptimizations_enableRailOffloading",
                c -> TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading,
                (c, v) -> TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading = v,
                OptionImpact.HIGH,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(createOptimizations));

        return new OptionPage(
                Component.translatable("config.tritium.category.techOptimizations"),
                ImmutableList.copyOf(groups)
        );
    }

    private OptionPage createFixesPage() {
        List<OptionGroup> groups = new ArrayList<>();

        List<TritiumOptionDefinition<?>> buttonFix = new ArrayList<>();
        buttonFix.add(new TritiumOptionDefinition<>(
                "fixes.buttonFix_buttonFix",
                "config.tritium.fixes.buttonFix_buttonFix",
                c -> TritiumConfigBase.Fixes.ButtonFix.buttonFix,
                (c, v) -> TritiumConfigBase.Fixes.ButtonFix.buttonFix = v,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(buttonFix));

        List<TritiumOptionDefinition<?>> noGLog = new ArrayList<>();
        noGLog.add(new TritiumOptionDefinition<>(
                "fixes.noGLog_noGLog",
                "config.tritium.fixes.noGLog_noGLog",
                c -> TritiumConfigBase.Fixes.NoGLog.noGLog,
                (c, v) -> TritiumConfigBase.Fixes.NoGLog.noGLog = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(noGLog));

        List<TritiumOptionDefinition<?>> memoryLeakFix = new ArrayList<>();
        memoryLeakFix.add(new TritiumOptionDefinition<>(
                "fixes.memoryLeakFix_AE2WTLibCreativeTabLeakFix",
                "config.tritium.fixes.memoryLeakFix_AE2WTLibCreativeTabLeakFix",
                c -> TritiumConfigBase.Fixes.MemoryLeakFix.AE2WTLibCreativeTabLeakFix,
                (c, v) -> TritiumConfigBase.Fixes.MemoryLeakFix.AE2WTLibCreativeTabLeakFix = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        memoryLeakFix.add(new TritiumOptionDefinition<>(
                "fixes.memoryLeakFix_ScreenshotByteBufferLeakFix",
                "config.tritium.fixes.memoryLeakFix_ScreenshotByteBufferLeakFix",
                c -> TritiumConfigBase.Fixes.MemoryLeakFix.ScreenshotByteBufferLeakFix,
                (c, v) -> TritiumConfigBase.Fixes.MemoryLeakFix.ScreenshotByteBufferLeakFix = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(memoryLeakFix));

        List<TritiumOptionDefinition<?>> beeFixes = new ArrayList<>();
        beeFixes.add(new TritiumOptionDefinition<>(
                "fixes.beeFixes_enableBeeFixes",
                "config.tritium.fixes.beeFixes_enableBeeFixes",
                c -> TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes,
                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes = v,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        beeFixes.add(new TritiumOptionDefinition<>(
                "fixes.beeFixes_fixWeatherInNether",
                "config.tritium.fixes.beeFixes_fixWeatherInNether",
                c -> TritiumConfigBase.Fixes.BeeFixes.fixWeatherInNether,
                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.fixWeatherInNether = v,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        beeFixes.add(new TritiumOptionDefinition<>(
                "fixes.beeFixes_fixBeeGravity",
                "config.tritium.fixes.beeFixes_fixBeeGravity",
                c -> TritiumConfigBase.Fixes.BeeFixes.fixBeeGravity,
                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.fixBeeGravity = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        beeFixes.add(new TritiumOptionDefinition<>(
                "fixes.beeFixes_fixBeeTurtleEgg",
                "config.tritium.fixes.beeFixes_fixBeeTurtleEgg",
                c -> TritiumConfigBase.Fixes.BeeFixes.fixBeeTurtleEgg,
                (c, v) -> TritiumConfigBase.Fixes.BeeFixes.fixBeeTurtleEgg = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(beeFixes));

        return new OptionPage(
                Component.translatable("config.tritium.category.fixes"),
                ImmutableList.copyOf(groups)
        );
    }

    private OptionPage createServerPerformancePage() {
        List<OptionGroup> groups = new ArrayList<>();

        List<TritiumOptionDefinition<?>> noiseSamplingCache = new ArrayList<>();
        noiseSamplingCache.add(new TritiumOptionDefinition<>(
                "serverPerformance.noiseSamplingCache_noiseSamplingCache",
                "config.tritium.serverPerformance.noiseSamplingCache_noiseSamplingCache",
                c -> TritiumConfigBase.ServerPerformance.NoiseSamplingCache.noiseSamplingCache,
                (c, v) -> TritiumConfigBase.ServerPerformance.NoiseSamplingCache.noiseSamplingCache = v,
                OptionImpact.LOW,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(noiseSamplingCache));

        List<TritiumOptionDefinition<?>> jigsawOptimizations = new ArrayList<>();
        jigsawOptimizations.add(new TritiumOptionDefinition<>(
                "serverPerformance.jigsawOptimizations_enableJigsawOptimizations",
                "config.tritium.serverPerformance.jigsawOptimizations_enableJigsawOptimizations",
                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations,
                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations = v,
                OptionImpact.HIGH,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        jigsawOptimizations.add(new TritiumOptionDefinition<>(
                "serverPerformance.jigsawOptimizations_enableOctreeCollisionDetection",
                "config.tritium.serverPerformance.jigsawOptimizations_enableOctreeCollisionDetection",
                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableOctreeCollisionDetection,
                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableOctreeCollisionDetection = v,
                OptionImpact.HIGH,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        jigsawOptimizations.add(new TritiumOptionDefinition<>(
                "serverPerformance.jigsawOptimizations_enableFastWeightedSampling",
                "config.tritium.serverPerformance.jigsawOptimizations_enableFastWeightedSampling",
                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableFastWeightedSampling,
                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableFastWeightedSampling = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        jigsawOptimizations.add(new TritiumOptionDefinition<>(
                "serverPerformance.jigsawOptimizations_enableStructureBlockFiltering",
                "config.tritium.serverPerformance.jigsawOptimizations_enableStructureBlockFiltering",
                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableStructureBlockFiltering,
                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableStructureBlockFiltering = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        jigsawOptimizations.add(new TritiumOptionDefinition<>(
                "serverPerformance.jigsawOptimizations_enableJigsawGenerationCheck",
                "config.tritium.serverPerformance.jigsawOptimizations_enableJigsawGenerationCheck",
                c -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawGenerationCheck,
                (c, v) -> TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawGenerationCheck = v,
                OptionImpact.MEDIUM,
                TritiumOptionDefinition.ControlType.BOOLEAN
        ));
        groups.add(createGroup(jigsawOptimizations));

        return new OptionPage(
                Component.translatable("config.tritium.category.serverPerformance"),
                ImmutableList.copyOf(groups)
        );
    }

    private OptionGroup createGroup(List<TritiumOptionDefinition<?>> definitions) {
        OptionGroup.Builder builder = OptionGroup.createBuilder();

        List<Option<?>> options = createOptionsFromDefinitions(definitions);
        for (Option<?> option : options) {
            builder.add(option);
        }

        return builder.build();
    }

    private List<Option<?>> createOptionsFromDefinitions(List<TritiumOptionDefinition<?>> definitions) {
        List<Option<?>> options = new ArrayList<>();

        for (TritiumOptionDefinition<?> definition : definitions) {
            Option<?> option = createOptionFromDefinition(definition);
            if (option != null) {
                options.add(option);
            }
        }

        return options;
    }

    private Option<?> createOptionFromDefinition(TritiumOptionDefinition<?> definition) {
        return switch (definition.getControlType()) {
            case BOOLEAN -> createBooleanOptionFromDefinition((TritiumOptionDefinition<Boolean>) definition);
            case INTEGER_SLIDER -> createIntSliderOptionFromDefinition((TritiumOptionDefinition<Integer>) definition);
            case DOUBLE_SLIDER -> createDoubleSliderOptionFromDefinition((TritiumOptionDefinition<Double>) definition);
        };
    }

    private OptionImpl<TritiumConfigBase, Boolean> createBooleanOptionFromDefinition(
            TritiumOptionDefinition<Boolean> definition) {

        String translationKey = definition.getTranslationKey();
        OptionImpl.Builder<TritiumConfigBase, Boolean> builder = OptionImpl.createBuilder(Boolean.class, storage)
                .setName(Component.translatable(translationKey))
                .setTooltip(Component.translatable(translationKey + ".tooltip"))
                .setControl(TickBoxControl::new)
                .setBinding(
                        (config, value) -> safeSetter(definition, config, value),
                        (config) -> safeGetter(definition, config)
                );

        if (definition.hasImpact()) {
            builder.setImpact(definition.getImpact());
        }

        return builder.build();
    }

    private OptionImpl<TritiumConfigBase, Integer> createIntSliderOptionFromDefinition(
            TritiumOptionDefinition<Integer> definition) {

        String translationKey = definition.getTranslationKey();
        Object[] params = definition.getControlParams();

        if (params.length < 3) {
            TritiumCommon.LOG.error("Invalid parameters for integer slider: {}", definition.getKey());
            return null;
        }

        int min = (int) params[0];
        int max = (int) params[1];
        int step = (int) params[2];

        ControlValueFormatter formatter = definition.getFormatter();
        if (formatter == null) {
            formatter = params.length > 3 ?
                    (ControlValueFormatter) params[3] : ControlValueFormatter.number();
        }

        ControlValueFormatter finalFormatter = formatter;
        OptionImpl.Builder<TritiumConfigBase, Integer> builder = OptionImpl.createBuilder(int.class, storage)
                .setName(Component.translatable(translationKey))
                .setTooltip(Component.translatable(translationKey + ".tooltip"))
                .setControl(opt -> new SliderControl(opt, min, max, step, finalFormatter))
                .setBinding(
                        (config, value) -> safeSetter(definition, config, value),
                        (config) -> safeGetter(definition, config)
                );

        if (definition.hasImpact()) {
            builder.setImpact(definition.getImpact());
        }

        return builder.build();
    }

    private OptionImpl<TritiumConfigBase, Integer> createDoubleSliderOptionFromDefinition(
            TritiumOptionDefinition<Double> definition) {

        String translationKey = definition.getTranslationKey();
        Object[] params = definition.getControlParams();

        if (params.length < 3) {
            TritiumCommon.LOG.error("Invalid parameters for double slider: {}", definition.getKey());
            return null;
        }

        double min = (double) params[0];
        double max = (double) params[1];
        double step = (double) params[2];

        int scaledMin = (int) (min * 10);
        int scaledMax = (int) (max * 10);
        int scaledStep = (int) (step * 10);

        OptionImpl.Builder<TritiumConfigBase, Integer> builder = OptionImpl.createBuilder(int.class, storage)
                .setName(Component.translatable(translationKey))
                .setTooltip(Component.translatable(translationKey + ".tooltip"))
                .setControl(opt -> new SliderControl(opt, scaledMin, scaledMax, scaledStep,
                        v -> Component.literal(String.format("%.1f", v / 10.0))))
                .setBinding(
                        (config, intValue) -> {
                            try {
                                double value = intValue / 10.0;
                                definition.getSetter().accept(config, value);
                            } catch (Exception e) {
                                TritiumCommon.LOG.error("Failed to set config value for {}", definition.getKey(), e);
                            }
                        },
                        (config) -> {
                            try {
                                double value = definition.getGetter().apply(config);
                                return (int) Math.round(value * 10);
                            } catch (Exception e) {
                                TritiumCommon.LOG.error("Failed to get config value for {}", definition.getKey(), e);
                                return scaledMin;
                            }
                        }
                );

        if (definition.hasImpact()) {
            builder.setImpact(definition.getImpact());
        }

        return builder.build();
    }

    private <T> void safeSetter(TritiumOptionDefinition<T> definition, TritiumConfigBase config, T value) {
        try {
            definition.getSetter().accept(config, value);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to set config value for {}", definition.getKey(), e);
        }
    }

    private <T> T safeGetter(TritiumOptionDefinition<T> definition, TritiumConfigBase config) {
        try {
            return definition.getGetter().apply(config);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to get config value for {}", definition.getKey(), e);
            return getDefaultValue(definition);
        }
    }

    private <T> T getDefaultValue(TritiumOptionDefinition<T> definition) {
        if (definition.getControlType() == TritiumOptionDefinition.ControlType.BOOLEAN) {
            return (T) Boolean.FALSE;
        } else if (definition.getControlType() == TritiumOptionDefinition.ControlType.INTEGER_SLIDER) {
            Object[] params = definition.getControlParams();
            return (T) Integer.valueOf((int) params[0]);
        }
        return null;
    }
}