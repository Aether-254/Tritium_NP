package org.craftamethyst.tritium.util.random;

import java.util.random.RandomGenerator;

final class RandomGeneratorAdapter implements TritiumRandom {
    private final RandomGenerator delegate;

    RandomGeneratorAdapter(RandomGenerator delegate) {
        this.delegate = delegate;
    }

    @Override
    public int nextInt() {
        return delegate.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return delegate.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return delegate.nextLong();
    }

    @Override
    public float nextFloat() {
        return delegate.nextFloat();
    }

    @Override
    public double nextDouble() {
        return delegate.nextDouble();
    }

    @Override
    public boolean nextBoolean() {
        return delegate.nextBoolean();
    }
}
