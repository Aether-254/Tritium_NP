package org.craftamethyst.tritium.util.octree;

public final class OctreeHolder {
    private static BoxOctree current;

    public static void set(BoxOctree tree) {
        current = tree;
    }

    public static BoxOctree get() {
        return current;
    }

    public static void clear() {
        current = null;
    }
}