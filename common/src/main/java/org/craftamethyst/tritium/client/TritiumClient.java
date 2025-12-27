package org.craftamethyst.tritium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.cull.AABBCullingManager;
import org.craftamethyst.tritium.cull.CullCache;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;
import org.craftamethyst.tritium.cull.iface.BlockEntityVisibility;

import java.util.List;

public class TritiumClient {
    public static TritiumClient instance;
    private final AABBCullingManager aabbCulling = new AABBCullingManager();
    private Vec3 lastCameraPos = Vec3.ZERO;
    private int framesSinceLastUpdate = 0;

    public TritiumClient() {
        instance = this;
    }

    public static void shutdown() {
        if (instance != null) {
            instance.aabbCulling.dispose();
        }
    }

    public boolean shouldSkipEntity(Entity e) {
        if (e == null) return false;
        if (!TritiumConfigBase.Rendering.EntityCulling.enableCulling) return false;
        if (isEntityBlacklisted(e)) return false;

        if (e == Minecraft.getInstance().getCameraEntity()) return false;
        if (e == Minecraft.getInstance().player) return false;
        if (e.isVehicle()) return false;
        if (e.isPassenger()) return false;
        if (e.hasCustomName()) return false;
        if (e.isCurrentlyGlowing()) return false;

        if (e instanceof EntityVisibility cullable) {
            return cullable.tritium$isCulled();
        }

        return false;
    }

    public boolean shouldSkipBlockEntity(BlockEntity be) {
        if (be == null) return false;
        if (!TritiumConfigBase.Rendering.EntityCulling.enableBlockEntityCulling) return false;

        if (be instanceof BlockEntityVisibility cullable) {
            return cullable.tritium$isCulled();
        }

        return false;
    }

    public void requestCullUpdate() {
        aabbCulling.requestCull();
    }

    public void clientTick() {
        requestCullUpdate();

        Minecraft mc = Minecraft.getInstance();
        Vec3 currentCameraPos = mc.gameRenderer.getMainCamera().getPosition();
        framesSinceLastUpdate++;

        if (framesSinceLastUpdate >= 20 || currentCameraPos.distanceToSqr(lastCameraPos) > 16.0) {
            lastCameraPos = currentCameraPos;
            aabbCulling.updateCameraPosition();
            framesSinceLastUpdate = 0;
        }
    }

    public CullCache getCullCache() {
        return aabbCulling.getCullCache();
    }

    public AABBCullingManager getAABBCullingManager() {
        return aabbCulling;
    }

    private boolean isEntityBlacklisted(Entity entity) {
        ResourceLocation entityId = EntityType.getKey(entity.getType());
        String entityName = entityId.toString();
        List<String> blacklist = TritiumConfigBase.Rendering.EntityCulling.entityBlacklist;

        for (String pattern : blacklist) {
            if (pattern.equals("*")) return true;
            if (pattern.endsWith(":*")) {
                String namespace = pattern.substring(0, pattern.length() - 2);
                if (entityName.startsWith(namespace + ":")) return true;
            }
            if (entityName.equals(pattern)) return true;
        }
        return false;
    }
}