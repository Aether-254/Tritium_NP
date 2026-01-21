package org.craftamethyst.tritium.util.random;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.random.RandomGenerator;

public class TritiumRandomSource implements RandomSource {
    private final String algorithm;
    private final int split;
    private final TritiumRandomTarget target;
    private RandomGenerator generator;

    public TritiumRandomSource(String algorithm, long seed, int split, TritiumRandomTarget target) {
        this.algorithm = algorithm;
        this.split = split;
        this.target = target;
        reseed(seed);
    }

    @Override
    public RandomSource fork() {
        return new TritiumRandomSource(algorithm, nextLong(), split, target);
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new TritiumPositionalRandomFactory(algorithm, nextLong(), split, target);
    }

    @Override
    public void setSeed(long seed) {
        reseed(seed);
    }

    @Override
    public int nextInt() {
        return generator.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return generator.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return generator.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return generator.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return generator.nextFloat();
    }

    @Override
    public double nextDouble() {
        return generator.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return generator.nextGaussian();
    }

    private void reseed(long seed) {
        RandomGenerator created = TritiumRandomManager.createGenerator(algorithm, seed, target);
        this.generator = TritiumRandomManager.applySplit(created, split);
    }
}
