package org.craftamethyst.tritium.cull;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.craftamethyst.tritium.cull.iface.BlockEntityVisibility;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;

import java.util.Iterator;

public class AABBCullingManager {
    private final OcclusionCullingInstance occlusionCulling;
    private final CullCache cullCache = new CullCache();
    private final Minecraft mc;
    private final Vec3d reusableAabbMin = new Vec3d(0, 0, 0);
    private final Vec3d reusableAabbMax = new Vec3d(0, 0, 0);
    private final Vec3d reusableCamera = new Vec3d(0, 0, 0);
    private final Vec3d lastCameraPos = new Vec3d(0, 0, 0);
    private Vec3 cachedCameraPos = Vec3.ZERO;
    private long lastCameraUpdate = 0;
    private Thread cullThread;
    private volatile boolean running = true;
    private volatile boolean requestCull = false;

    public AABBCullingManager() {
        this.mc = Minecraft.getInstance();
        this.occlusionCulling = new OcclusionCullingInstance(128, new OcclusionProvider());
        startCullThread();
    }

    private void startCullThread() {
        cullThread = new Thread(this::cullLoop, "Tritium-Culling-Thread");
        cullThread.setDaemon(true);
        cullThread.setPriority(Thread.MIN_PRIORITY + 1);
        cullThread.start();
    }

    private void cullLoop() {
        while (running && mc.isRunning()) {
            try {
                Thread.sleep(33);
                if (requestCull || !isCameraStationary()) {
                    requestCull = false;
                    updateCameraCache();
                    Vec3 cameraPos = getCameraPos();
                    lastCameraPos.set(cameraPos.x, cameraPos.y, cameraPos.z);

                    occlusionCulling.resetCache();
                    cullEntities(cameraPos);
                    cullBlockEntities(cameraPos);
                }
            }  catch (InterruptedException | NullPointerException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                running = false;
                break;
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    }

    private void updateCameraCache() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCameraUpdate > 50) {
            Camera mainCamera = mc.gameRenderer.getMainCamera();
            cachedCameraPos = mainCamera.getPosition();
            lastCameraUpdate = currentTime;
        }
    }

    private boolean isCameraStationary() {
        Vec3 currentPos = getCameraPos();
        return currentPos.x == lastCameraPos.x &&
                currentPos.y == lastCameraPos.y &&
                currentPos.z == lastCameraPos.z;
    }

    private void cullEntities(Vec3 cameraPos) {
        if (mc.level == null || mc.player == null) return;

        try {
            Iterator<Entity> iterator = mc.level.entitiesForRendering().iterator();
            while (iterator.hasNext()) {
                try {
                    Entity entity = iterator.next();
                    if (!(entity instanceof EntityVisibility cullable)) {
                        continue;
                    }

                    if (cullable.tritium$isForcedVisible()) {
                        cullable.tritium$setCulled(false);
                        cullCache.cacheEntity(entity, false);
                        continue;
                    }

                    CullCache.CullResult cached = cullCache.checkEntity(entity);
                    if (cached.isCached()) {
                        cullable.tritium$setCulled(cached.isCulled());
                        continue;
                    }

                    if (entity.isCurrentlyGlowing() || isSkippableArmorstand(entity) ||
                            entity == mc.player || entity == mc.cameraEntity) {
                        cullable.tritium$setCulled(false);
                        cullCache.cacheEntity(entity, false);
                        continue;
                    }

                    // if (!entity.position().closerThan(cameraPos, 128)) {
                    //     cullable.tritium$setCulled(true);
                    //     cullCache.cacheEntity(entity, false);
                    //     continue;
                    // }

                    AABB boundingBox = entity.getBoundingBox();
                    if (boundingBox.getXsize() > 50 || boundingBox.getYsize() > 50 || boundingBox.getZsize() > 50) {
                        cullable.tritium$setCulled(false);
                        cullCache.cacheEntity(entity, false);
                        continue;
                    }

                    reusableAabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                    reusableAabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                    reusableCamera.set(cameraPos.x, cameraPos.y, cameraPos.z);
                    boolean visible = occlusionCulling.isAABBVisible(reusableAabbMin, reusableAabbMax, reusableCamera);
                    boolean shouldCull = !visible;

                    cullable.tritium$setCulled(shouldCull);
                    cullCache.cacheEntity(entity, shouldCull);

                } catch (NullPointerException e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void cullBlockEntities(Vec3 cameraPos) {
        if (mc.level == null || mc.player == null) return;

        int chunkRadius = 8;
        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                try {
                    var chunk = mc.level.getChunk(mc.player.chunkPosition().x + x, mc.player.chunkPosition().z + z);
                    for (var entry : chunk.getBlockEntities().entrySet()) {
                        BlockEntity blockEntity = entry.getValue();
                        if (!(blockEntity instanceof BlockEntityVisibility cullable)) {
                            continue;
                        }

                        if (cullable.tritium$isForcedVisible()) {
                            cullable.tritium$setCulled(false);
                            cullCache.cacheBlockEntity(blockEntity, false);
                            continue;
                        }

                        CullCache.CullResult cached = cullCache.checkBlockEntity(blockEntity);
                        if (cached.isCached()) {
                            cullable.tritium$setCulled(cached.isCulled());
                            continue;
                        }

                        BlockPos pos = entry.getKey();

                        // if (!blockCenter.closerThan(cameraPos, 64)) {
                        //     cullable.tritium$setCulled(true);
                        //     cullCache.cacheBlockEntity(blockEntity, false);
                        //     continue;
                        // }

                        AABB boundingBox;
                        if (blockEntity.hasLevel()) {
                            var blockState = blockEntity.getBlockState();
                            VoxelShape shape = null;
                            if (blockEntity.getLevel() != null) {
                                shape = blockState.getCollisionShape(blockEntity.getLevel(), pos);
                            }
                            assert shape != null;
                            if (!shape.isEmpty()) {
                                boundingBox = shape.bounds().move(pos);
                            } else {
                                boundingBox = new AABB(pos);
                            }
                        } else {
                            boundingBox = new AABB(pos);
                        }

                        String className = blockEntity.getClass().getName();
                        if (className.contains("GunSmithTableBlockEntity")) {
                            cullable.tritium$setCulled(false);
                            cullCache.cacheBlockEntity(blockEntity, false);
                            continue;
                        }

                        double maxSize = 16.0;
                        if (boundingBox.getXsize() > maxSize || boundingBox.getYsize() > maxSize || boundingBox.getZsize() > maxSize) {
                            cullable.tritium$setCulled(false);
                            cullCache.cacheBlockEntity(blockEntity, false);
                            continue;
                        }

                        reusableAabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                        reusableAabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                        reusableCamera.set(cameraPos.x, cameraPos.y, cameraPos.z);
                        boolean visible = occlusionCulling.isAABBVisible(reusableAabbMin, reusableAabbMax, reusableCamera);
                        boolean shouldCull = !visible;

                        cullable.tritium$setCulled(shouldCull);
                        cullCache.cacheBlockEntity(blockEntity, shouldCull);
                    }
                } catch (Exception e) {
                }
            }
        }
    }
    private boolean isSkippableArmorstand(Entity entity) {
        return entity instanceof ArmorStand && ((ArmorStand) entity).isMarker();
    }

    public void requestCull() {
        requestCull = true;
    }

    public void updateCameraPosition() {
        lastCameraUpdate = 0;
        cullCache.clear();
        requestCull = true;
    }

    private Vec3 getCameraPos() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCameraUpdate > 50) {
            Camera mainCamera = mc.gameRenderer.getMainCamera();
            cachedCameraPos = mainCamera.getPosition();
            lastCameraUpdate = currentTime;
        }
        return cachedCameraPos;
    }

    public void dispose() {
        running = false;
        if (cullThread != null) {
            cullThread.interrupt();
        }
        cullCache.clear();
    }

    public CullCache getCullCache() {
        return cullCache;
    }
}