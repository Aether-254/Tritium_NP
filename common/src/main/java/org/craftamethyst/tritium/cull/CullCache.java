package org.craftamethyst.tritium.cull;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CullCache {
    private final Int2BooleanOpenHashMap entityCullCache = new Int2BooleanOpenHashMap(65536);
    private final Long2BooleanOpenHashMap blockEntityCullCache = new Long2BooleanOpenHashMap(8192);
    private final Long2LongOpenHashMap entityCacheTimestamps = new Long2LongOpenHashMap(65536);
    private final Long2LongOpenHashMap blockEntityCacheTimestamps = new Long2LongOpenHashMap(8192);
    private final CullResult cachedTrueResult = new CullResult(true, true);
    private final CullResult cachedFalseResult = new CullResult(true, false);
    private final CullResult uncachedResult = new CullResult(false, false);

    public CullResult checkEntity(Entity entity) {
        if (entity == null) return uncachedResult;

        int entityId = entity.getId();
        long currentTime = System.currentTimeMillis();

        if (entityCacheTimestamps.containsKey(entityId)) {
            long lastTime = entityCacheTimestamps.get(entityId);

            if (currentTime - lastTime < 250) {
                boolean culled = entityCullCache.get(entityId);
                return culled ? cachedTrueResult : cachedFalseResult;
            }
        }

        return uncachedResult;
    }

    public void cacheEntity(Entity entity, boolean culled) {
        if (entity == null) return;

        int entityId = entity.getId();
        if (entityId < 0) return;

        long currentTime = System.currentTimeMillis();
        entityCullCache.put(entityId, culled);
        entityCacheTimestamps.put(entityId, currentTime);
    }

    public CullResult checkBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null) return uncachedResult;

        long blockPos = blockEntity.getBlockPos().asLong();
        long currentTime = System.currentTimeMillis();

        if (blockEntityCacheTimestamps.containsKey(blockPos)) {
            long lastTime = blockEntityCacheTimestamps.get(blockPos);

            if (currentTime - lastTime < 500) {
                boolean culled = blockEntityCullCache.get(blockPos);
                return culled ? cachedTrueResult : cachedFalseResult;
            }
        }

        return uncachedResult;
    }

    public void cacheBlockEntity(BlockEntity blockEntity, boolean culled) {
        if (blockEntity == null) return;

        long blockPos = blockEntity.getBlockPos().asLong();
        long currentTime = System.currentTimeMillis();
        blockEntityCullCache.put(blockPos, culled);
        blockEntityCacheTimestamps.put(blockPos, currentTime);
    }

    public void clear() {
        entityCullCache.clear();
        blockEntityCullCache.clear();
        entityCacheTimestamps.clear();
        blockEntityCacheTimestamps.clear();
    }

    public static class CullResult {
        private final boolean cached;
        private final boolean culled;

        public CullResult(boolean cached, boolean culled) {
            this.cached = cached;
            this.culled = culled;
        }

        public boolean isCached() {
            return cached;
        }

        public boolean isCulled() {
            return culled;
        }
    }
}