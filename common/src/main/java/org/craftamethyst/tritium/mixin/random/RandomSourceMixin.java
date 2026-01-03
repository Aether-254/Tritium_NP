package org.craftamethyst.tritium.mixin.random;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;
import org.craftamethyst.tritium.random.TritiumRandomManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RandomSource.class)
public interface RandomSourceMixin {

    /**
     * @author QianMo0721
     * @reason Allow global RNG override
     */
    @Overwrite
    static RandomSource create() {
        RandomSource custom = TritiumRandomManager.createMinecraftRandomSource(null, false);
        if (custom != null) {
            return custom;
        }
        return new LegacyRandomSource(RandomSupport.generateUniqueSeed());
    }

    /**
     * @author QianMo0721
     * @reason Allow global RNG override
     */
    @Overwrite
    static RandomSource createThreadSafe() {
        RandomSource custom = TritiumRandomManager.createMinecraftRandomSource(null, true);
        if (custom != null) {
            return custom;
        }
        return new ThreadSafeLegacyRandomSource(RandomSupport.generateUniqueSeed());
    }

    /**
     * @author QianMo0721
     * @reason Allow global RNG override
     */
    @Overwrite
    static RandomSource create(long seed) {
        RandomSource custom = TritiumRandomManager.createMinecraftRandomSource(seed, false);
        if (custom != null) {
            return custom;
        }
        return new LegacyRandomSource(seed);
    }

    /**
     * @author QianMo0721
     * @reason Allow global RNG override
     */
    @Overwrite
    static RandomSource createNewThreadLocalInstance() {
        RandomSource custom = TritiumRandomManager.createMinecraftRandomSource(null, true);
        if (custom != null) {
            return custom;
        }
        return new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong());
    }
}
