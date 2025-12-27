package org.craftamethyst.tritium.util.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Set;

public abstract class AbstractCachedPackResources extends AbstractPackResources {
    private final IResourceCache cache;
    public static File file;
    public AbstractCachedPackResources(String name, boolean builtin, IResourceCache cache) {
        super(name, builtin);
        this.cache = cache;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String @NotNull ... parts) {
        byte[] data = cache.getRootResource(parts);
        return data == null ? null : () -> new ByteArrayInputStream(data);
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(@NotNull PackType type, @NotNull ResourceLocation loc) {
        byte[] data = cache.getResource(type, loc);
        return data == null ? null : () -> new ByteArrayInputStream(data);
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
        return cache.getNamespaces(type);
    }

    @Override
    public void listResources(@NotNull PackType type, @NotNull String ns, @NotNull String path, @NotNull ResourceOutput out) {
        cache.listResources(type, ns, path, (l, d) -> out.accept(l, () -> new ByteArrayInputStream(d)));
    }

    @Override
    public void close() {
        cache.clear();
    }
}