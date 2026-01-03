package org.craftamethyst.tritium.util.random;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public final class TritiumPositionalRandomFactory implements PositionalRandomFactory {
    private final String algorithm;
    private final long baseSeed;
    private final int split;
    private final TritiumRandomTarget target;

    public TritiumPositionalRandomFactory(String algorithm, long baseSeed, int split, TritiumRandomTarget target) {
        this.algorithm = algorithm;
        this.baseSeed = baseSeed;
        this.split = split;
        this.target = target;
    }

    @Override
    public RandomSource fromHashOf(String name) {
        long hash = name != null ? name.hashCode() : 0L;
        return fromSeed(hash);
    }

    @Override
    public RandomSource fromSeed(long seed) {
        long mixed = TritiumRandomManager.mixSeed(baseSeed, seed);
        return new TritiumRandomSource(algorithm, mixed, split, target);
    }

    @Override
    public RandomSource at(int x, int y, int z) {
        long salt = (long) x * 341873128712L ^ (long) y * 132897987541L ^ (long) z * 42317861L;
        long mixed = TritiumRandomManager.mixSeed(baseSeed, salt);
        return new TritiumRandomSource(algorithm, mixed, split, target);
    }

    @Override
    public void parityConfigString(StringBuilder builder) {
        builder.append("TritiumRandom(")
                .append(algorithm)
                .append(",split=")
                .append(split)
                .append(")");
    }
}
