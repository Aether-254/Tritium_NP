package org.craftamethyst.tritium.util.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IResourceCache {
    byte @Nullable [] getResource(PackType type, ResourceLocation location);

    byte @Nullable [] getRootResource(String... parts);

    Set<String> getNamespaces(PackType type);

    void listResources(PackType type, String namespace, String path, ResourceConsumer consumer);

    void clear();

    @FunctionalInterface
    interface ResourceConsumer {
        void accept(ResourceLocation location, byte[] data);
    }
}