package org.craftamethyst.tritium.mixin.occfix;

import com.logisticscraft.occlusionculling.cache.ArrayOcclusionCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ArrayOcclusionCache.class, remap = false)
public abstract class ArrayOcclusionCacheMixin {

    @Shadow @Final private int reachX2;
    @Shadow @Final private byte[] cache;
    @Shadow private int positionKey;
    @Shadow private int entry;
    @Shadow private int offset;

    @Overwrite
    public void setVisible(int x, int y, int z) {
        if (x < 0 || x >= reachX2 || y < 0 || y >= reachX2 || z < 0 || z >= reachX2) {
            return;
        }

        positionKey = x + y * reachX2 + z * reachX2 * reachX2;
        if (positionKey < 0 || positionKey >= cache.length * 4) {
            return;
        }

        entry = positionKey / 4;
        offset = (positionKey % 4) * 2;

        if (entry < 0 || entry >= cache.length) {
            return;
        }

        cache[entry] |= 1 << offset;
    }

    @Overwrite
    public void setHidden(int x, int y, int z) {
        if (x < 0 || x >= reachX2 || y < 0 || y >= reachX2 || z < 0 || z >= reachX2) {
            return;
        }

        positionKey = x + y * reachX2 + z * reachX2 * reachX2;
        if (positionKey < 0 || positionKey >= cache.length * 4) {
            return;
        }

        entry = positionKey / 4;
        offset = (positionKey % 4) * 2;

        if (entry < 0 || entry >= cache.length) {
            return;
        }

        cache[entry] |= 1 << offset + 1;
    }

    @Overwrite
    public int getState(int x, int y, int z) {
        if (x < 0 || x >= reachX2 || y < 0 || y >= reachX2 || z < 0 || z >= reachX2) {
            return 0;
        }

        positionKey = x + y * reachX2 + z * reachX2 * reachX2;
        if (positionKey < 0 || positionKey >= cache.length * 4) {
            return 0;
        }

        entry = positionKey / 4;
        offset = (positionKey % 4) * 2;

        if (entry < 0 || entry >= cache.length) {
            return 0;
        }

        return (cache[entry] >> offset) & 3;
    }

    @Overwrite
    public void setLastVisible() {
        if (entry < 0 || entry >= cache.length) {
            return;
        }
        cache[entry] |= 1 << offset;
    }

    @Overwrite
    public void setLastHidden() {
        if (entry < 0 || entry >= cache.length) {
            return;
        }
        cache[entry] |= 1 << offset + 1;
    }
}