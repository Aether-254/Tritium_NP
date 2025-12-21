package org.craftamethyst.tritium.cull;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AABBCullingManager {
    private final OcclusionCullingInstance occlusionCulling;
    private final CullCache cullCache = new CullCache();
    private final Minecraft mc;
    private final AABBOBJ reusableAABB = new AABBOBJ(0, 0, 0, 0, 0, 0);
    private final Vec3d reusableAabbMin = new Vec3d(0, 0, 0);
    private final Vec3d reusableAabbMax = new Vec3d(0, 0, 0);
    private final Vec3d reusableCamera = new Vec3d(0, 0, 0);
    private Vec3 cachedCameraPos = Vec3.ZERO;
    private double cachedCullingDistance = 16.0;
    private long lastCameraUpdate = 0;
    private long lastDistanceUpdate = 0;

    public AABBCullingManager() {
        this.mc = Minecraft.getInstance();
        this.occlusionCulling = new OcclusionCullingInstance(16, new OcclusionProvider());
    }

    public boolean shouldCullEntity(Entity entity) {
        if (entity == null || mc.level == null) return false;

        CullCache.CullResult cached = cullCache.checkEntity(entity);
        if (cached.isCached()) {
            return cached.isCulled();
        }

        if (!needsDetailedEntityCheck(entity)) {
            cullCache.cacheEntity(entity, false);
            return false;
        }

        Vec3 cameraPos = getCachedCameraPos();
        double cullingDistance = getCachedCullingDistance();
        Vec3 entityPos = entity.getEyePosition();

        double dx = entityPos.x - cameraPos.x;
        double dy = entityPos.y - cameraPos.y;
        double dz = entityPos.z - cameraPos.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;

        if (distanceSq > (cullingDistance * cullingDistance)) {
            cullCache.cacheEntity(entity, true);
            return true;
        }

        AABB boundingBox = entity.getBoundingBox();
        if (isLargeEntity(boundingBox)) {
            cullCache.cacheEntity(entity, false);
            return false;
        }

        boolean visible = performOcclusionCheck(boundingBox, cameraPos);
        boolean shouldCull = !visible;
        cullCache.cacheEntity(entity, shouldCull);
        return shouldCull;
    }

    public boolean shouldCullBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null || mc.level == null) return false;

        CullCache.CullResult cached = cullCache.checkBlockEntity(blockEntity);
        if (cached.isCached()) {
            return cached.isCulled();
        }

        Vec3 cameraPos = getCachedCameraPos();
        double cullingDistance = getCachedCullingDistance();
        Vec3 blockPos = blockEntity.getBlockPos().getCenter();

        double dx = blockPos.x - cameraPos.x;
        double dy = blockPos.y - cameraPos.y;
        double dz = blockPos.z - cameraPos.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;

        if (distanceSq > (cullingDistance * cullingDistance)) {
            cullCache.cacheBlockEntity(blockEntity, true);
            return true;
        }

        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        reusableAABB.set(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

        boolean visible = performOcclusionCheck(reusableAABB, cameraPos);
        boolean shouldCull = !visible;
        cullCache.cacheBlockEntity(blockEntity, shouldCull);
        return shouldCull;
    }

    private boolean needsDetailedEntityCheck(Entity entity) {
        if (entity.isInvisible()) return false;
        if (entity.isSpectator()) return false;
        if (entity instanceof ArmorStand armorStand && armorStand.isMarker()) return false;
        return true;
    }

    private boolean performOcclusionCheck(AABB boundingBox, Vec3 cameraPos) {
        reusableAabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        reusableAabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        reusableCamera.set(cameraPos.x, cameraPos.y, cameraPos.z);
        return occlusionCulling.isAABBVisible(reusableAabbMin, reusableAabbMax, reusableCamera);
    }

    private boolean performOcclusionCheck(AABBOBJ boundingBox, Vec3 cameraPos) {
        reusableAabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        reusableAabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        reusableCamera.set(cameraPos.x, cameraPos.y, cameraPos.z);
        return occlusionCulling.isAABBVisible(reusableAabbMin, reusableAabbMax, reusableCamera);
    }

    private boolean isLargeEntity(AABB boundingBox) {
        return boundingBox.getXsize() > 10.0 || boundingBox.getYsize() > 10.0 || boundingBox.getZsize() > 10.0;
    }

    private Vec3 getCachedCameraPos() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCameraUpdate > 50) {
            Camera mainCamera = mc.gameRenderer.getMainCamera();
            cachedCameraPos = mainCamera.getPosition();
            lastCameraUpdate = currentTime;
        }
        return cachedCameraPos;
    }

    private double getCachedCullingDistance() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDistanceUpdate > 1000) {
            cachedCullingDistance = mc.level == null ? 64.0 : 48.0;
            lastDistanceUpdate = currentTime;
        }
        return cachedCullingDistance;
    }

    public void updateCameraPosition() {
        occlusionCulling.resetCache();
        lastCameraUpdate = 0;
    }

    public void dispose() {
        cullCache.clear();
    }

    public double getCurrentCullingDistance() {
        return cachedCullingDistance;
    }

    public OcclusionCullingInstance getOcclusionCulling() {
        return occlusionCulling;
    }

    public void forceResetCache() {
        occlusionCulling.resetCache();
    }

    public CullCache getCullCache() {
        return cullCache;
    }

    public static class AABBOBJ {
        public double minX, minY, minZ;
        public double maxX, maxY, maxZ;

        public AABBOBJ(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            set(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public void set(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public double getXsize() {
            return maxX - minX;
        }

        public double getYsize() {
            return maxY - minY;
        }

        public double getZsize() {
            return maxZ - minZ;
        }
    }
}