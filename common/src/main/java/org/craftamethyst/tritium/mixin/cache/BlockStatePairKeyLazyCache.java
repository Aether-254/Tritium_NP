package org.craftamethyst.tritium.mixin.cache;

import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;

@Mixin(targets = "net.minecraft.world.level.block.Block$ShapePairKey")
public class BlockStatePairKeyLazyCache {
    @Shadow
    @Final
    private VoxelShape first;

    @Shadow
    @Final
    private VoxelShape second;

    @Unique
    private int tritium$cachedHash = 0;

    @Unique
    private boolean tritium$isHashComputed = false;

    /**
     * @author ZCRAFT
     * @reason BlockStatePairKey hashCode
     */
    @Overwrite
    public int hashCode() {
        if (!tritium$isHashComputed) {
            tritium$computeAndCacheHashCode();
        }
        return tritium$cachedHash;
    }

    @Unique
    private void tritium$computeAndCacheHashCode() {
        int firstHash = System.identityHashCode(this.first);
        int secondHash = System.identityHashCode(this.second);

        this.tritium$cachedHash = firstHash * 31 + secondHash;
        this.tritium$isHashComputed = true;
    }
}