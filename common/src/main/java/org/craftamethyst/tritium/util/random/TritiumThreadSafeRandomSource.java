package org.craftamethyst.tritium.util.random;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.concurrent.atomic.AtomicLong;

public final class TritiumThreadSafeRandomSource implements RandomSource {
    private final String algorithm;
    private final int split;
    private final TritiumRandomTarget target;
    private final AtomicLong seed;
    private final AtomicLong epoch = new AtomicLong();
    private final ThreadLocal<SeededRandom> local = new ThreadLocal<>();

    public TritiumThreadSafeRandomSource(String algorithm, long seed, int split, TritiumRandomTarget target) {
        this.algorithm = algorithm;
        this.split = split;
        this.target = target;
        this.seed = new AtomicLong(seed);
    }

    @Override
    public RandomSource fork() {
        return new TritiumThreadSafeRandomSource(algorithm, nextLong(), split, target);
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new TritiumPositionalRandomFactory(algorithm, nextLong(), split, target);
    }

    @Override
    public void setSeed(long seed) {
        this.seed.set(seed);
        epoch.incrementAndGet();
        local.remove();
    }

    @Override
    public int nextInt() {
        return local().nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return local().nextInt(bound);
    }

    @Override
    public long nextLong() {
        return local().nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return local().nextBoolean();
    }

    @Override
    public float nextFloat() {
        return local().nextFloat();
    }

    @Override
    public double nextDouble() {
        return local().nextDouble();
    }

    @Override
    public double nextGaussian() {
        return local().nextGaussian();
    }

    private TritiumRandomSource local() {
        SeededRandom cached = local.get();
        long currentEpoch = epoch.get();
        if (cached == null || cached.epoch != currentEpoch) {
            long baseSeed = seed.get();
            long mixedSeed = TritiumRandomManager.mixSeed(baseSeed, Thread.currentThread().getId());
            TritiumRandomSource rng = new TritiumRandomSource(algorithm, mixedSeed, split, target);
            cached = new SeededRandom(currentEpoch, rng);
            local.set(cached);
        }
        return cached.random;
    }

    private record SeededRandom(long epoch, TritiumRandomSource random) {
    }
}
