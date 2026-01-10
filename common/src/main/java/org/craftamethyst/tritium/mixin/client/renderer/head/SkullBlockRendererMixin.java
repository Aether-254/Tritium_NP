package org.craftamethyst.tritium.mixin.client.renderer.head;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.util.cache.SoftRenderTypeReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {

    @Shadow
    @Final
    private static Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE;

    @Unique
    private static final LoadingCache<String, SoftRenderTypeReference> TRITIUM_PLAYER_CACHE;

    @Unique
    private static final Map<SkullBlock.Type, RenderType> TRITIUM_SKULL_RENDER_TYPES;

    static {
        Map<SkullBlock.Type, RenderType> temp = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<SkullBlock.Type, ResourceLocation> entry : SkullBlockRendererMixin.SKIN_BY_TYPE.entrySet()) {
            if (entry.getKey() != SkullBlock.Types.PLAYER) {
                temp.put(entry.getKey(), RenderType.entityCutoutNoCullZOffset(entry.getValue()));
            }
        }
        TRITIUM_SKULL_RENDER_TYPES = Collections.unmodifiableMap(temp);

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

    /**
     * @author ZCRAFT
     * @reason Optimize
     */
    @Overwrite
    public static RenderType getRenderType(SkullBlock.Type type, @Nullable ResolvableProfile profile) {
        if (type != SkullBlock.Types.PLAYER) {
            RenderType renderType = TRITIUM_SKULL_RENDER_TYPES.get(type);
            return renderType != null ? renderType : RenderType.entityCutoutNoCullZOffset(SKIN_BY_TYPE.get(type));
        }

        if (profile == null) {
            return RenderType.entityCutoutNoCullZOffset(DefaultPlayerSkin.getDefaultTexture());
        }

        String cacheKey = tritium$generateTextureHash(profile);

        try {
            SoftRenderTypeReference ref = TRITIUM_PLAYER_CACHE.getIfPresent(cacheKey);
            if (ref != null && ref.renderType != null) {
                return ref.renderType;
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Cache error, falling back to direct creation", e);
        }

        Minecraft mc = Minecraft.getInstance();
        SkinManager skinManager = mc.getSkinManager();
        ResourceLocation skinTexture = skinManager.getInsecureSkin(profile.gameProfile()).texture();

        RenderType renderType;
        boolean isDefaultSkin = skinTexture.equals(DefaultPlayerSkin.getDefaultTexture());

        if (isDefaultSkin) {
            renderType = RenderType.entityCutoutNoCullZOffset(skinTexture);
        } else {
            renderType = RenderType.entityTranslucent(skinTexture);
            TRITIUM_PLAYER_CACHE.put(cacheKey, new SoftRenderTypeReference(cacheKey, renderType));
        }

        return renderType;
    }

    @Unique
    private static String tritium$generateTextureHash(@Nullable ResolvableProfile profile) {
        if (profile == null || profile.properties() == null) {
            return "default_skin";
        }

        var properties = profile.properties().get("textures");
        if (properties != null && !properties.isEmpty()) {
            var property = properties.iterator().next();
            String textureUrl = property.value();
            int hash = textureUrl.hashCode();
            return Integer.toHexString(hash) + "_" + Integer.toHexString(textureUrl.length());
        }
        return "default_skin";
    }
}