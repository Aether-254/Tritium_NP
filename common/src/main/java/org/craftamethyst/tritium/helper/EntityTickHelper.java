package org.craftamethyst.tritium.helper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import me.zcraft.tconfig.config.TritiumConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public final class EntityTickHelper {
    private static final AtomicReference<Set<EntityType<?>>> WHITE_LIST = new AtomicReference<>(Collections.emptySet());
    private static final List<WildcardPattern> WHITE_PATTERNS = new ArrayList<>();
    private static final boolean ignoreDeadEntities = true;

    static {
        try {
            TritiumConfig config = TritiumConfig.getConfig("tritium");
            config.addReloadListener(EntityTickHelper::reloadConfig);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to register config reload listener", e);
        }

        reloadConfig();
    }


    public static boolean shouldSkipTick(Entity entity) {
        if (!TritiumConfigBase.Entities.EntityOpt.optimizeEntities) return false;
        if (!(entity instanceof LivingEntity living)) return false;

        if (!living.isAlive()) {
            return ignoreDeadEntities;
        }

        EntityType<?> type = entity.getType();
        if (matchesWildcard(type, WHITE_PATTERNS) || WHITE_LIST.get().contains(type)) {
            return false;
        }
        if (TritiumConfigBase.Entities.EntityOpt.tickRaidersInRaid && isRaiderInRaid(living)) return false;

        return !isNearPlayer(living);
    }
    
    public static boolean shouldSkipRender(Entity entity) {
        if (!TritiumConfigBase.Entities.EntityOpt.optimizeEntities) return false;
        if (!(entity instanceof LivingEntity living)) return false;

        if (!living.isAlive()) {
            return ignoreDeadEntities;
        }

        EntityType<?> type = entity.getType();
        if (matchesWildcard(type, WHITE_PATTERNS) || WHITE_LIST.get().contains(type)) {
            return false;
        }
        if (TritiumConfigBase.Entities.EntityOpt.tickRaidersInRaid && isRaiderInRaid(living)) return false;

        return !isNearPlayerForRender(living);
    }
    
    private static boolean isNearPlayerForRender(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        
        int renderHorizontalRange = TritiumConfigBase.Entities.EntityOpt.horizontalRange * 2;
        int renderVerticalRange = TritiumConfigBase.Entities.EntityOpt.verticalRange * 2;

        AABB box = new AABB(
                pos.getX() - renderHorizontalRange,
                pos.getY() - renderVerticalRange,
                pos.getZ() - renderHorizontalRange,
                pos.getX() + renderHorizontalRange,
                pos.getY() + renderVerticalRange,
                pos.getZ() + renderHorizontalRange
        );

        for (Player player : level.players()) {
            if (!player.isAlive()) continue;
            if (player.getBoundingBox().intersects(box)) return true;
        }
        return false;
    }

    private static void reloadConfig() {
        List<String> whiteRaw = TritiumConfigBase.Entities.EntityOpt.entityWhitelist;
        Set<EntityType<?>> whiteIds = Sets.newHashSet();
        WHITE_PATTERNS.clear();

        whiteRaw.forEach(s -> parseEntry(s, whiteIds));
        WHITE_LIST.set(ImmutableSet.copyOf(whiteIds));
    }

    private static void parseEntry(String raw, Set<EntityType<?>> idTarget) {
        if (raw.contains("*") || raw.contains("?")) {
            WHITE_PATTERNS.add(new WildcardPattern(raw));
        } else {
            ResourceLocation key = ResourceLocation.tryParse(raw);
            if (key != null) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
                idTarget.add(type);
            }
        }
    }

    private static boolean matchesWildcard(EntityType<?> type, List<WildcardPattern> list) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String str = id.toString();
        for (WildcardPattern p : list) if (p.matches(str)) return true;
        return false;
    }

    private static boolean isRaiderInRaid(LivingEntity e) {
        return e instanceof net.minecraft.world.entity.raid.Raider raider && raider.isAlive() && raider.hasActiveRaid();
    }

    private static boolean isNearPlayer(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();

        AABB box = new AABB(
                pos.getX() - TritiumConfigBase.Entities.EntityOpt.horizontalRange,
                pos.getY() - TritiumConfigBase.Entities.EntityOpt.verticalRange,
                pos.getZ() - TritiumConfigBase.Entities.EntityOpt.horizontalRange,
                pos.getX() + TritiumConfigBase.Entities.EntityOpt.horizontalRange,
                pos.getY() + TritiumConfigBase.Entities.EntityOpt.verticalRange,
                pos.getZ() + TritiumConfigBase.Entities.EntityOpt.horizontalRange
        );

        for (Player player : level.players()) {
            if (!player.isAlive()) continue;
            if (player.getBoundingBox().intersects(box)) return true;
        }
        return false;
    }

    private static final class WildcardPattern {
        private final Pattern regex;

        WildcardPattern(String raw) {
            String s = raw.replace("?", ".{1}").replace("*", ".*");
            this.regex = Pattern.compile("^" + s + "$");
        }

        boolean matches(String str) {
            return regex.matcher(str).matches();
        }
    }
}