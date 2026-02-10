package org.craftamethyst.tritium.mixin.client.renderer.culling;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.api.EntityRendererAccessor;
import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderEntity",
            at = @At("HEAD"),
            cancellable = true)
    private void tritium$skipCulledEntityButMaybeRenderNameTag(
            Entity entity, double cameraX, double cameraY, double cameraZ,
            float tickDelta, PoseStack matrices, MultiBufferSource consumers,
            CallbackInfo ci) {
        if (!TritiumConfigBase.Rendering.EntityCulling.enableCulling) return;
        TritiumClient client = TritiumClient.instance;
        if (client == null || !(entity instanceof EntityVisibility cullable)) return;
        if (cullable.tritium$isForcedVisible() || entity.isSpectator()) {
            cullable.tritium$setOutOfCamera(false);
            return;
        }

        cullable.tritium$setOutOfCamera(false);
    }
}