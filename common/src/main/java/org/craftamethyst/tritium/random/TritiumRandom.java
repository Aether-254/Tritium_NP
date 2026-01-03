package org.craftamethyst.tritium.random;

public interface TritiumRandom {
    int nextInt();

    int nextInt(int bound);

    long nextLong();

    float nextFloat();

    double nextDouble();

    boolean nextBoolean();
}
