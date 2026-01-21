package org.craftamethyst.tritium.util.create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

public final class CreateRenderBounds {
    private CreateRenderBounds() {
    }

    public static AABB clampToLocal(AABB original, BlockPos pos, double inflate) {
        return clampTo(original, new AABB(pos).inflate(inflate));
    }

    public static AABB clampTo(AABB original, AABB cap) {
        if (original == null) {
            return cap;
        }
        if (original.getXsize() <= cap.getXsize()
                && original.getYsize() <= cap.getYsize()
                && original.getZsize() <= cap.getZsize()) {
            return original;
        }
        return cap;
    }

    public static AABB beltBounds(BlockPos pos, Vec3i direction, int length, double inflate) {
        if (direction == null) {
            return new AABB(pos).inflate(inflate);
        }
        int steps = Math.max(1, length);
        BlockPos end = pos.offset(direction.getX() * steps, direction.getY() * steps, direction.getZ() * steps);
        return new AABB(pos).minmax(new AABB(end)).inflate(inflate);
    }

    public static AABB chainConveyorBounds(BlockPos pos, Iterable<BlockPos> connections, double inflate) {
        AABB box = new AABB(pos);
        if (connections != null) {
            for (BlockPos connection : connections) {
                if (connection != null) {
                    box = box.minmax(new AABB(connection));
                }
            }
        }
        return box.inflate(inflate);
    }

    public static AABB frogportBounds(BlockPos pos, double inflate) {
        return new AABB(pos).expandTowards(0.0, 1.0, 0.0).inflate(inflate);
    }
}
