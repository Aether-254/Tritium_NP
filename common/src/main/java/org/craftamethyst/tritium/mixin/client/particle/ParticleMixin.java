package org.craftamethyst.tritium.mixin.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;

@Mixin(value = Particle.class,priority = 499)
public abstract class ParticleMixin {
    @Shadow
    private boolean stoppedByCollision;
    @Shadow protected boolean hasPhysics;
    @Shadow protected boolean onGround;
    @Shadow protected double xd;
    @Shadow protected double zd;
    @Shadow protected double x;
    @Shadow protected double y;
    @Shadow protected double z;
    @Final
    @Shadow protected ClientLevel level;
    @Shadow protected float bbHeight;

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract void setBoundingBox(AABB box);

    @Shadow
    protected abstract void setLocationFromBoundingbox();

    @Shadow
    public abstract void setPos(double pX, double pY, double pZ);

    @Unique
    private final BlockPos.MutableBlockPos tritium$cachedPos1 = new BlockPos.MutableBlockPos();
    @Unique
    private final BlockPos.MutableBlockPos tritium$cachedPos2 = new BlockPos.MutableBlockPos();
    @Unique
    private final BlockPos.MutableBlockPos tritium$cachedPos3 = new BlockPos.MutableBlockPos();
    @Unique
    private boolean tritium$cachedSolid1;
    @Unique
    private boolean tritium$cachedSolid2;
    @Unique
    private boolean tritium$cachedSolid3;

    /**
     * @author ZCRAFT
     * @reason optimize
     */
    @Overwrite
    public void move(double pX, double pY, double pZ) {
        if (this.stoppedByCollision) {
            return;
        }

        double epsilon = 1.0E-7;
        if (Math.abs(pX) < epsilon && Math.abs(pY) < epsilon && Math.abs(pZ) < epsilon) {
            return;
        }

        if (!this.hasPhysics) {
            this.setPos(this.x + pX, this.y + pY, this.z + pZ);
            return;
        }

        double originalX = pX;
        double originalY = pY;
        double originalZ = pZ;

        AABB currentBox = this.getBoundingBox();

        pY = tritium$verticalCollision(currentBox, originalY);

        this.onGround = originalY < 0 && Math.abs(pY) < Math.abs(originalY);
        if (Math.abs(originalY) >= 1.0E-5 && Math.abs(pY) < 1.0E-5) {
            this.stoppedByCollision = true;
        }

        if (originalX != 0) {
            AABB afterYMove = currentBox.move(0, pY, 0);
            pX = tritium$horizontalCollision(afterYMove, originalX, Direction.Axis.X);
            if (Math.abs(pX) < Math.abs(originalX)) {
                this.xd = 0.0;
            }
        }

        if (originalZ != 0) {
            AABB afterXYMove = currentBox.move(pX, pY, 0);
            pZ = tritium$horizontalCollision(afterXYMove, originalZ, Direction.Axis.Z);
            if (Math.abs(pZ) < Math.abs(originalZ)) {
                this.zd = 0.0;
            }
        }

        if (pX != 0 || pY != 0 || pZ != 0) {
            this.setBoundingBox(currentBox.move(pX, pY, pZ));
            this.setLocationFromBoundingbox();
        }
    }

    @Unique
    private double tritium$verticalCollision(AABB box, double originalY) {
        if (originalY == 0) return 0;

        boolean movingDown = originalY < 0;
        double distance = Math.abs(originalY);

        double checkY;
        double centerX = (box.minX + box.maxX) * 0.5;
        double centerZ = (box.minZ + box.maxZ) * 0.5;
        if (movingDown) {
            checkY = box.minY - distance;

            if (tritium$solidAt(centerX, checkY, centerZ)) {
                return -Math.max(0, box.minY - Math.ceil(checkY) - 0.001);
            }

            double corner1X = box.minX + 0.25;
            double corner1Z = box.minZ + 0.25;
            double corner2X = box.maxX - 0.25;
            double corner2Z = box.maxZ - 0.25;

            boolean hit1 = tritium$solidAt(corner1X, checkY, corner1Z);
            boolean hit2 = tritium$solidAt(corner2X, checkY, corner2Z);

            if (hit1 || hit2) {
                double minDistance = distance;
                double collisionY = Math.ceil(checkY);
                double d = box.minY - collisionY - 0.001;
                minDistance = Math.min(minDistance, d);
                return -minDistance;
            }

        } else {
            checkY = box.maxY + distance;

            if (tritium$solidAt(centerX, checkY, centerZ)) {
                return Math.max(0, Math.floor(checkY) - box.maxY - 0.001);
            }

        }
        return originalY;
    }

    @Unique
    private double tritium$horizontalCollision(AABB box, double originalMove, Direction.Axis axis) {
        if (originalMove == 0) return 0;

        boolean positive = originalMove > 0;
        double distance = Math.abs(originalMove);

        if (axis == Direction.Axis.X) {
            double checkX = positive ? box.maxX + distance : box.minX - distance;
            double centerY = box.minY + this.bbHeight * 0.5;
            double centerZ = (box.minZ + box.maxZ) * 0.5;

            if (tritium$solidAt(checkX, centerY, centerZ)) {
                double collisionPoint = positive ?
                        Math.ceil(checkX) - box.maxX - 0.001 :
                        box.minX - Math.floor(checkX) - 0.001;
                return positive ? collisionPoint : -collisionPoint;
            }

            double topY = box.maxY - 0.15;
            double bottomY = box.minY + 0.15;

            boolean hitTop = tritium$solidAt(checkX, topY, centerZ);
            boolean hitBottom = tritium$solidAt(checkX, bottomY, centerZ);

            if (hitTop || hitBottom) {
                double collisionPoint = positive ?
                        Math.ceil(checkX) - box.maxX - 0.001 :
                        box.minX - Math.floor(checkX) - 0.001;
                return positive ? collisionPoint : -collisionPoint;
            }

            return originalMove;
        } else {
            double checkZ = positive ? box.maxZ + distance : box.minZ - distance;
            double centerY = box.minY + this.bbHeight * 0.5;
            double centerX = (box.minX + box.maxX) * 0.5;

            if (tritium$solidAt(centerX, centerY, checkZ)) {
                double collisionPoint = positive ?
                        Math.ceil(checkZ) - box.maxZ - 0.001 :
                        box.minZ - Math.floor(checkZ) - 0.001;
                return positive ? collisionPoint : -collisionPoint;
            }

            double topY = box.maxY - 0.15;
            double bottomY = box.minY + 0.15;

            boolean hitTop = tritium$solidAt(centerX, topY, checkZ);
            boolean hitBottom = tritium$solidAt(centerX, bottomY, checkZ);

            if (hitTop || hitBottom) {
                double collisionPoint = positive ?
                        Math.ceil(checkZ) - box.maxZ - 0.001 :
                        box.minZ - Math.floor(checkZ) - 0.001;
                return positive ? collisionPoint : -collisionPoint;
            }

            return originalMove;
        }
    }

    @Unique
    private boolean tritium$solidAt(double x, double y, double z) {
        int blockX = Mth.floor(x);
        int blockY = Mth.floor(y);
        int blockZ = Mth.floor(z);

        if (blockY < this.level.getMinBuildHeight() || blockY >= this.level.getMaxBuildHeight()) {
            return false;
        }

        if (tritium$cachedPos1.getX() == blockX && tritium$cachedPos1.getY() == blockY && tritium$cachedPos1.getZ() == blockZ) {
            return tritium$cachedSolid1;
        }

        if (tritium$cachedPos2.getX() == blockX && tritium$cachedPos2.getY() == blockY && tritium$cachedPos2.getZ() == blockZ) {
            return tritium$cachedSolid2;
        }

        if (tritium$cachedPos3.getX() == blockX && tritium$cachedPos3.getY() == blockY && tritium$cachedPos3.getZ() == blockZ) {
            return tritium$cachedSolid3;
        }

        BlockPos pos = new BlockPos(blockX, blockY, blockZ);
        BlockState state = this.level.getBlockState(pos);

        if (state.isAir()) {
            tritium$updateCache(blockX, blockY, blockZ, false);
            return false;
        }

        VoxelShape collisionShape = state.getCollisionShape(this.level, pos);
        boolean hasCollision = !collisionShape.isEmpty();

        tritium$updateCache(blockX, blockY, blockZ, hasCollision);
        return hasCollision;
    }

    @Unique
    private void tritium$updateCache(int x, int y, int z, boolean solid) {
        tritium$cachedPos1.set(tritium$cachedPos2);
        tritium$cachedSolid1 = tritium$cachedSolid2;

        tritium$cachedPos2.set(tritium$cachedPos3);
        tritium$cachedSolid2 = tritium$cachedSolid3;

        tritium$cachedPos3.set(x, y, z);
        tritium$cachedSolid3 = solid;
    }
}