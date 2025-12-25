package org.craftamethyst.tritium.util.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

public abstract class AbstractCachedPackResources extends AbstractPackResources {
    protected final IResourceCache resourceCache;

    protected AbstractCachedPackResources(PackLocationInfo info, IResourceCache cache) {
        super(info);
        this.resourceCache = cache;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... parts) {
        byte[] data = resourceCache.getRootResource(parts);
        return data != null ? () -> new ByteArrayInputStream(data) : null;
    }

    @Override
    public IoSupplier<InputStream> getResource(@NotNull PackType type,
                                               @NotNull ResourceLocation location) {
        byte[] data = resourceCache.getResource(type, location);
        return data != null ? () -> new ByteArrayInputStream(data) : null;
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
        return resourceCache.getNamespaces(type);
    }

    @Override
    public void listResources(@NotNull PackType type, @NotNull String namespace,
                              @NotNull String path, @NotNull ResourceOutput output) {
        resourceCache.listResources(type, namespace, path,
                (location, data) -> output.accept(location, () -> new ByteArrayInputStream(data)));
    }

    @Override
    public void close() {
        resourceCache.clear();
    }
}