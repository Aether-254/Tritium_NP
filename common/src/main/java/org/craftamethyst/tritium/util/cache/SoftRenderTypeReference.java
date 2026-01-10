package org.craftamethyst.tritium.util.cache;

import net.minecraft.client.renderer.RenderType;

public class SoftRenderTypeReference {
    public final String cacheKey;
    public final RenderType renderType;

    public SoftRenderTypeReference(String cacheKey, RenderType renderType) {
        this.cacheKey = cacheKey;
        this.renderType = renderType;
    }
}