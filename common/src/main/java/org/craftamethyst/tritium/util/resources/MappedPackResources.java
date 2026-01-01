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

public class MappedPackResources extends AbstractPackResources {
    protected final IResourceCache resourceMapper;
    public static File file;

    protected MappedPackResources(String name, boolean builtin, IResourceCache mapper) {
        super(name, builtin);
        this.resourceMapper = mapper;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... parts) {
        byte[] data = resourceMapper.getRootResource(parts);
        return data != null ? () -> new ByteArrayInputStream(data) : null;
    }

    @Override
    public IoSupplier<InputStream> getResource(@NotNull PackType type,
                                               @NotNull ResourceLocation location) {
        byte[] data = resourceMapper.getResource(type, location);
        return data != null ? () -> new ByteArrayInputStream(data) : null;
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
        return resourceMapper.getNamespaces(type);
    }

    @Override
    public void listResources(@NotNull PackType type, @NotNull String namespace,
                              @NotNull String path, @NotNull ResourceOutput output) {
        resourceMapper.listResources(type, namespace, path,
                (location, data) -> output.accept(location, () -> new ByteArrayInputStream(data)));
    }

    @Override
    public void close() {
        resourceMapper.clear();
    }
}