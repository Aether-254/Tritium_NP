package org.craftamethyst.tritium.mixin.client.renderer.culling;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.cull.CullCache;
import org.craftamethyst.tritium.cull.RenderCacheEntry;
import org.craftamethyst.tritium.helper.EntityTickHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Unique
    private static final Int2ObjectOpenHashMap<RenderCacheEntry> RENDER_CACHE = new Int2ObjectOpenHashMap<>();
    @Unique
    private static final long CACHE_DURATION_INVISIBLE = 300;

    @Inject(
            method = "shouldRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends Entity> void tritium$earlyCullingCheck(
            E entity, Frustum frustum, double camX, double camY, double camZ,
            CallbackInfoReturnable<Boolean> cir) {

        int entityId = entity.getId();

        if (TritiumConfigBase.Entities.EntityOpt.ite) {
            RenderCacheEntry cacheEntry = RENDER_CACHE.get(entityId);
            if (cacheEntry != null && !cacheEntry.isExpired()) {
                cir.setReturnValue(cacheEntry.isShouldRender());
                return;
            }

            if (EntityTickHelper.shouldSkipTick(entity)) {
                RENDER_CACHE.put(entityId, new RenderCacheEntry(false, CACHE_DURATION_INVISIBLE));
                cir.setReturnValue(false);
                return;
            }
        }

        if (TritiumConfigBase.Rendering.EntityCulling.enableCulling) {
            TritiumClient client = TritiumClient.instance;
            if (client == null) return;

            CullCache.CullResult cached = client.getCullCache().checkEntity(entity);
            if (cached.isCached() && cached.isCulled()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
            method = "shouldRender",
            at = @At("TAIL"),
            cancellable = true
    )
    private <E extends Entity> void tritium$skipCulledOrTickSkippedEntity(
            E entity, Frustum frustum, double camX, double camY, double camZ,
            CallbackInfoReturnable<Boolean> cir) {
        boolean result = cir.getReturnValue();
        if (result) {
            TritiumClient client = TritiumClient.instance;
            if (client != null && client.shouldSkipEntity(entity)) {
                cir.setReturnValue(false);
            }
        }
    }
}