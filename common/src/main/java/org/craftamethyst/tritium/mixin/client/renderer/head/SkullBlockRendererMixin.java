package org.craftamethyst.tritium.mixin.client.renderer.head;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.util.cache.SoftRenderTypeReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {

    @Shadow
    @Final
    private static Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE;

    @Unique
    private static final Map<SkullBlock.Type, RenderType> TRITIUM_SKULL_RENDER_TYPES;

    @Unique
    private static final LoadingCache<String, SoftRenderTypeReference> TRITIUM_PLAYER_CACHE;

    static {
        Map<SkullBlock.Type, RenderType> temp = Maps.newHashMap();
        for (Map.Entry<SkullBlock.Type, ResourceLocation> entry : SKIN_BY_TYPE.entrySet()) {
            if (entry.getKey() != SkullBlock.Types.PLAYER) {
                temp.put(entry.getKey(), RenderType.entityCutoutNoCullZOffset(entry.getValue()));
            }
        }
        TRITIUM_SKULL_RENDER_TYPES = Maps.newHashMap(temp);

        TRITIUM_PLAYER_CACHE = CacheBuilder.newBuilder()
                .maximumWeight(100 * 1024 * 1024)
                .weigher((String key, SoftRenderTypeReference ref) -> {
                    int weight = key.length() * 2;
                    weight += 32;
                    weight += 24;
                    return weight;
                })
                .softValues()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .expireAfterWrite(2, TimeUnit.HOURS)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .refreshAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull SoftRenderTypeReference load(@NotNull String cacheKey) {
                        return new SoftRenderTypeReference(cacheKey, null);
                    }
                });
    }

    @Inject(
            method = "getRenderType",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tritium$getRenderType(SkullBlock.Type skullType, @Nullable GameProfile gameProfile,
                                              CallbackInfoReturnable<RenderType> cir) {
        if (skullType != SkullBlock.Types.PLAYER) {
            RenderType renderType = TRITIUM_SKULL_RENDER_TYPES.get(skullType);
            if (renderType != null) {
                cir.setReturnValue(renderType);
                return;
            }
        }

        if (skullType == SkullBlock.Types.PLAYER && gameProfile != null) {
            String cacheKey = tritium$generateTextureHash(gameProfile);

            try {
                SoftRenderTypeReference ref = TRITIUM_PLAYER_CACHE.getIfPresent(cacheKey);
                if (ref != null && ref.renderType != null) {
                    cir.setReturnValue(ref.renderType);
                }
            } catch (Exception e) {
                TritiumCommon.LOG.error("Cache error, falling back to direct creation", e);
            }
        }
    }

    @Unique
    private static String tritium$generateTextureHash(@Nullable GameProfile profile) {
        if (profile == null) {
            return "default_skin";
        }

        Minecraft minecraft = Minecraft.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures =
                minecraft.getSkinManager().getInsecureSkinInformation(profile);

        if (textures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            MinecraftProfileTexture texture = textures.get(MinecraftProfileTexture.Type.SKIN);
            if (texture != null && texture.getUrl() != null) {
                String textureUrl = texture.getUrl();
                int hash = textureUrl.hashCode();
                UUID uuid = profile.getId();
                String uuidStr = uuid != null ? uuid.toString() : UUIDUtil.getOrCreatePlayerUUID(profile).toString();
                return uuidStr + "_" + Integer.toHexString(hash);
            }
        }

        return "default_skin_" + UUIDUtil.getOrCreatePlayerUUID(profile);
    }

    @Inject(
            method = "getRenderType",
            at = @At("RETURN")
    )
    private static void tritium$cachePlayerRenderType(SkullBlock.Type skullType, @Nullable GameProfile gameProfile,
                                                      CallbackInfoReturnable<RenderType> cir) {
        if (skullType == SkullBlock.Types.PLAYER && gameProfile != null && cir.getReturnValue() != null) {
            RenderType renderType = cir.getReturnValue();

            if (!renderType.equals(RenderType.entityCutoutNoCullZOffset(
                    DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(gameProfile))))) {
                String cacheKey = tritium$generateTextureHash(gameProfile);
                TRITIUM_PLAYER_CACHE.put(cacheKey, new SoftRenderTypeReference(cacheKey, renderType));
            }
        }
    }
}