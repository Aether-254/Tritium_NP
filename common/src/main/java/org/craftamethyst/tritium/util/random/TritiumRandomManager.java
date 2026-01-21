package org.craftamethyst.tritium.util.random;

import me.zcraft.tconfig.config.TritiumConfig;
import net.minecraft.util.RandomSource;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.EnumMap;
import java.util.Locale;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.random.RandomGenerator;
import java.util.random.RandomGenerator.SplittableGenerator;
import java.util.random.RandomGeneratorFactory;

public final class TritiumRandomManager {
    private static volatile EnumMap<TritiumRandomTarget, RandomState> STATES = new EnumMap<>(TritiumRandomTarget.class);
    private static volatile GlobalState GLOBAL_STATE;

    static {
        try {
            TritiumConfig config = TritiumConfig.getConfig("tritium");
            config.addReloadListener(TritiumRandomManager::reloadConfig);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to register FastRandom config reload listener", e);
        }
        reloadConfig();
    }

    private TritiumRandomManager() {
    }

    public static void reloadConfig() {
        boolean globalEnable = TritiumConfigBase.FastRandom.enableFastRandom;
        TritiumConfigBase.FastRandom.Algorithm defaultAlgorithm = TritiumConfigBase.FastRandom.defaultAlgorithm;
        long defaultSeed = TritiumConfigBase.FastRandom.defaultSeed;
        TritiumConfigBase.FastRandom.StreamMode defaultStreamMode = TritiumConfigBase.FastRandom.defaultStreamMode;
        int defaultSplit = Math.max(0, TritiumConfigBase.FastRandom.defaultSplit);

        EnumMap<TritiumRandomTarget, RandomState> next = new EnumMap<>(TritiumRandomTarget.class);
        next.put(
                TritiumRandomTarget.STRUCTURE_POOL_WEIGHTED_SAMPLE,
                buildState(
                        TritiumRandomTarget.STRUCTURE_POOL_WEIGHTED_SAMPLE,
                        globalEnable,
                        TritiumConfigBase.FastRandom.StructurePoolWeightedSample.enable,
                        TritiumConfigBase.FastRandom.StructurePoolWeightedSample.algorithm,
                        TritiumConfigBase.FastRandom.StructurePoolWeightedSample.seed,
                        TritiumConfigBase.FastRandom.StructurePoolWeightedSample.streamMode,
                        TritiumConfigBase.FastRandom.StructurePoolWeightedSample.split,
                        defaultAlgorithm,
                        defaultSeed,
                        defaultStreamMode,
                        defaultSplit
                )
        );

        next.put(
                TritiumRandomTarget.PARTICLE_REJECTION,
                buildState(
                        TritiumRandomTarget.PARTICLE_REJECTION,
                        globalEnable,
                        TritiumConfigBase.FastRandom.ParticleRejection.enable,
                        TritiumConfigBase.FastRandom.ParticleRejection.algorithm,
                        TritiumConfigBase.FastRandom.ParticleRejection.seed,
                        TritiumConfigBase.FastRandom.ParticleRejection.streamMode,
                        TritiumConfigBase.FastRandom.ParticleRejection.split,
                        defaultAlgorithm,
                        defaultSeed,
                        defaultStreamMode,
                        defaultSplit
                )
        );

        STATES = next;
        GLOBAL_STATE = buildGlobalState(
                globalEnable,
                TritiumConfigBase.FastRandom.MinecraftGlobal.enable,
                TritiumConfigBase.FastRandom.MinecraftGlobal.algorithm,
                TritiumConfigBase.FastRandom.MinecraftGlobal.seed,
                TritiumConfigBase.FastRandom.MinecraftGlobal.streamMode,
                TritiumConfigBase.FastRandom.MinecraftGlobal.split,
                defaultAlgorithm,
                defaultSeed,
                defaultStreamMode,
                defaultSplit
        );
    }

    public static int nextInt(TritiumRandomTarget target, RandomSource vanillaRandom, int bound) {
        RandomState state = STATES.get(target);
        if (state == null || !state.isEnabled()) {
            return fallbackNextInt(vanillaRandom, bound);
        }
        TritiumRandom random = state.get(vanillaRandom);
        if (random == null) {
            return fallbackNextInt(vanillaRandom, bound);
        }
        return random.nextInt(bound);
    }

    public static double nextDouble(TritiumRandomTarget target, RandomSource vanillaRandom) {
        RandomState state = STATES.get(target);
        if (state == null || !state.isEnabled()) {
            return fallbackNextDouble(vanillaRandom);
        }
        TritiumRandom random = state.get(vanillaRandom);
        if (random == null) {
            return fallbackNextDouble(vanillaRandom);
        }
        return random.nextDouble();
    }

    public static float nextFloat(TritiumRandomTarget target, RandomSource vanillaRandom) {
        RandomState state = STATES.get(target);
        if (state == null || !state.isEnabled()) {
            return fallbackNextFloat(vanillaRandom);
        }
        TritiumRandom random = state.get(vanillaRandom);
        if (random == null) {
            return fallbackNextFloat(vanillaRandom);
        }
        return random.nextFloat();
    }

    public static boolean nextBoolean(TritiumRandomTarget target, RandomSource vanillaRandom) {
        RandomState state = STATES.get(target);
        if (state == null || !state.isEnabled()) {
            return fallbackNextBoolean(vanillaRandom);
        }
        TritiumRandom random = state.get(vanillaRandom);
        if (random == null) {
            return fallbackNextBoolean(vanillaRandom);
        }
        return random.nextBoolean();
    }

    public static RandomSource createMinecraftRandomSource(Long seedFromCall, boolean forceThreadLocal) {
        GlobalState state = GLOBAL_STATE;
        if (state == null || !state.enabled()) {
            return null;
        }
        return state.create(seedFromCall, forceThreadLocal);
    }

    private static RandomState buildState(
            TritiumRandomTarget target,
            boolean globalEnable,
            boolean targetEnable,
            TritiumConfigBase.FastRandom.Algorithm targetAlgorithm,
            long targetSeed,
            TritiumConfigBase.FastRandom.StreamMode targetStreamMode,
            int targetSplit,
            TritiumConfigBase.FastRandom.Algorithm defaultAlgorithm,
            long defaultSeed,
            TritiumConfigBase.FastRandom.StreamMode defaultStreamMode,
            int defaultSplit
    ) {
        boolean enabled = globalEnable && targetEnable;
        TritiumConfigBase.FastRandom.Algorithm resolvedAlgorithm = resolveAlgorithm(targetAlgorithm, defaultAlgorithm);
        boolean useVanilla = isVanillaAlgorithm(resolvedAlgorithm);
        String algorithm = algorithmName(resolvedAlgorithm);
        long seed = targetSeed != 0L ? targetSeed : defaultSeed;
        boolean hasSeed = targetSeed != 0L || defaultSeed != 0L;
        StreamMode streamMode = resolveStreamMode(targetStreamMode, defaultStreamMode);
        int split = targetSplit > 0 ? targetSplit : defaultSplit;

        return new RandomState(target, enabled && !useVanilla, algorithm, seed, hasSeed, streamMode, split);
    }

    private static GlobalState buildGlobalState(
            boolean globalEnable,
            boolean targetEnable,
            TritiumConfigBase.FastRandom.Algorithm targetAlgorithm,
            long targetSeed,
            TritiumConfigBase.FastRandom.StreamMode targetStreamMode,
            int targetSplit,
            TritiumConfigBase.FastRandom.Algorithm defaultAlgorithm,
            long defaultSeed,
            TritiumConfigBase.FastRandom.StreamMode defaultStreamMode,
            int defaultSplit
    ) {
        boolean enabled = globalEnable && targetEnable;
        TritiumConfigBase.FastRandom.Algorithm resolvedAlgorithm = resolveAlgorithm(targetAlgorithm, defaultAlgorithm);
        boolean useVanilla = isVanillaAlgorithm(resolvedAlgorithm);
        String algorithm = algorithmName(resolvedAlgorithm);
        long seed = targetSeed != 0L ? targetSeed : defaultSeed;
        boolean hasSeed = targetSeed != 0L || defaultSeed != 0L;
        StreamMode streamMode = resolveStreamMode(targetStreamMode, defaultStreamMode);
        int split = targetSplit > 0 ? targetSplit : defaultSplit;

        return new GlobalState(enabled && !useVanilla, algorithm, seed, hasSeed, streamMode, split);
    }

    private static TritiumConfigBase.FastRandom.Algorithm resolveAlgorithm(
            TritiumConfigBase.FastRandom.Algorithm targetAlgorithm,
            TritiumConfigBase.FastRandom.Algorithm defaultAlgorithm
    ) {
        if (targetAlgorithm == null || targetAlgorithm == TritiumConfigBase.FastRandom.Algorithm.DEFAULT) {
            return defaultAlgorithm;
        }
        return targetAlgorithm;
    }

    private static boolean isVanillaAlgorithm(TritiumConfigBase.FastRandom.Algorithm algorithm) {
        return algorithm == TritiumConfigBase.FastRandom.Algorithm.VANILLA
                || algorithm == TritiumConfigBase.FastRandom.Algorithm.INHERIT;
    }

    private static StreamMode resolveStreamMode(
            TritiumConfigBase.FastRandom.StreamMode targetStreamMode,
            TritiumConfigBase.FastRandom.StreamMode defaultStreamMode
    ) {
        TritiumConfigBase.FastRandom.StreamMode resolved = targetStreamMode;
        if (resolved == null || resolved == TritiumConfigBase.FastRandom.StreamMode.DEFAULT) {
            resolved = defaultStreamMode;
        }
        if (resolved == TritiumConfigBase.FastRandom.StreamMode.THREAD_LOCAL) {
            return StreamMode.THREAD_LOCAL;
        }
        return StreamMode.SHARED;
    }

    private static String algorithmName(TritiumConfigBase.FastRandom.Algorithm algorithm) {
        if (algorithm == null || algorithm == TritiumConfigBase.FastRandom.Algorithm.DEFAULT) {
            return "L64X128MixRandom";
        }
        return switch (algorithm) {
            case VANILLA, INHERIT -> "";
            case RANDOM -> "Random";
            case SPLITABLERANDOM -> "SplittableRandom";
            case L64X128MIXRANDOM -> "L64X128MixRandom";
            case L64X256MIXRANDOM -> "L64X256MixRandom";
            case L128X128MIXRANDOM -> "L128X128MixRandom";
            case L128X256MIXRANDOM -> "L128X256MixRandom";
            case L64X1024MIXRANDOM -> "L64X1024MixRandom";
            case L128X1024MIXRANDOM -> "L128X1024MixRandom";
            case XOROSHIRO128PLUSPLUS -> "Xoroshiro128PlusPlus";
            case XOROSHIRO128STARSTAR -> "Xoroshiro128StarStar";
            case XOSHIRO256PLUSPLUS -> "Xoshiro256PlusPlus";
            case XOSHIRO256STARSTAR -> "Xoshiro256StarStar";
            case DEFAULT -> "L64X128MixRandom";
        };
    }

    private static int fallbackNextInt(RandomSource vanillaRandom, int bound) {
        if (vanillaRandom != null) {
            return vanillaRandom.nextInt(bound);
        }
        return ThreadLocalRandom.current().nextInt(bound);
    }

    private static double fallbackNextDouble(RandomSource vanillaRandom) {
        if (vanillaRandom != null) {
            return vanillaRandom.nextDouble();
        }
        return ThreadLocalRandom.current().nextDouble();
    }

    private static float fallbackNextFloat(RandomSource vanillaRandom) {
        if (vanillaRandom != null) {
            return vanillaRandom.nextFloat();
        }
        return ThreadLocalRandom.current().nextFloat();
    }

    private static boolean fallbackNextBoolean(RandomSource vanillaRandom) {
        if (vanillaRandom != null) {
            return vanillaRandom.nextBoolean();
        }
        return ThreadLocalRandom.current().nextBoolean();
    }

    static String normalizeAlgorithmName(String algorithm) {
        String trimmed = algorithm.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "random" -> "Random";
            case "splittable", "splittablerandom" -> "SplittableRandom";
            case "l64x128mix", "l64x128mixrandom" -> "L64X128MixRandom";
            case "l64x256mix", "l64x256mixrandom" -> "L64X256MixRandom";
            case "l128x128mix", "l128x128mixrandom" -> "L128X128MixRandom";
            case "l128x256mix", "l128x256mixrandom" -> "L128X256MixRandom";
            case "l64x1024mix", "l64x1024mixrandom" -> "L64X1024MixRandom";
            case "l128x1024mix", "l128x1024mixrandom" -> "L128X1024MixRandom";
            case "xoroshiro128plusplus" -> "Xoroshiro128PlusPlus";
            case "xoroshiro128starstar" -> "Xoroshiro128StarStar";
            case "xoshiro256plusplus" -> "Xoshiro256PlusPlus";
            case "xoshiro256starstar" -> "Xoshiro256StarStar";
            default -> trimmed;
        };
    }

    static RandomGenerator createGenerator(String algorithm, long seed, TritiumRandomTarget target) {
        if (algorithm == null || algorithm.isBlank()) {
            return new SplittableRandom(seed);
        }
        String normalized = normalizeAlgorithmName(algorithm);
        try {
            return RandomGeneratorFactory.of(normalized).create(seed);
        } catch (IllegalArgumentException e) {
            TritiumCommon.LOG.warn("FastRandom: RNG algorithm {} not available for {}, using SplittableRandom.", normalized, target);
            return new SplittableRandom(seed);
        }
    }

    static RandomGenerator applySplit(RandomGenerator generator, int splitCount) {
        if (splitCount <= 0) {
            return generator;
        }
        RandomGenerator current = generator;
        if (!(current instanceof SplittableGenerator)) {
            current = new SplittableRandom(current.nextLong());
        }
        SplittableGenerator splittable = (SplittableGenerator) current;
        for (int i = 0; i < splitCount; i++) {
            splittable = splittable.split();
        }
        return splittable;
    }

    static long mixSeed(long seed, long salt) {
        long mixed = seed ^ (salt * 0x9E3779B97F4A7C15L);
        mixed ^= (mixed >>> 33);
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= (mixed >>> 33);
        mixed *= 0xC4CEB9FE1A85EC53L;
        return mixed ^ (mixed >>> 33);
    }

    private enum StreamMode {
        SHARED,
        THREAD_LOCAL
    }

    private static final class RandomState {
        private final TritiumRandomTarget target;
        private final boolean enabled;
        private final String algorithm;
        private final long seed;
        private final boolean hasSeed;
        private final StreamMode streamMode;
        private final int split;
        private final AtomicReference<TritiumRandom> shared = new AtomicReference<>();
        private final ThreadLocal<TritiumRandom> threadLocal = new ThreadLocal<>();

        private RandomState(TritiumRandomTarget target, boolean enabled, String algorithm, long seed, boolean hasSeed,
                            StreamMode streamMode, int split) {
            this.target = target;
            this.enabled = enabled;
            this.algorithm = algorithm;
            this.seed = seed;
            this.hasSeed = hasSeed;
            this.streamMode = streamMode;
            this.split = split;
        }

        boolean isEnabled() {
            return enabled;
        }

        TritiumRandom get(RandomSource vanillaRandom) {
            if (!enabled) {
                return null;
            }
            return switch (streamMode) {
                case THREAD_LOCAL -> getThreadLocal(vanillaRandom);
                case SHARED -> getShared(vanillaRandom);
            };
        }

        private TritiumRandom getShared(RandomSource vanillaRandom) {
            TritiumRandom rng = shared.get();
            if (rng != null) {
                return rng;
            }
            long seedValue = resolveSeed(vanillaRandom);
            TritiumRandom created = createRandom(seedValue);
            if (shared.compareAndSet(null, created)) {
                return created;
            }
            return shared.get();
        }

        private TritiumRandom getThreadLocal(RandomSource vanillaRandom) {
            TritiumRandom rng = threadLocal.get();
            if (rng != null) {
                return rng;
            }
            long seedValue = resolveSeed(vanillaRandom);
            seedValue = mixSeed(seedValue, Thread.currentThread().getId());
            TritiumRandom created = createRandom(seedValue);
            threadLocal.set(created);
            return created;
        }

        private long resolveSeed(RandomSource vanillaRandom) {
            if (hasSeed) {
                return seed;
            }
            if (vanillaRandom != null) {
                return vanillaRandom.nextLong();
            }
            return ThreadLocalRandom.current().nextLong();
        }

        private TritiumRandom createRandom(long seedValue) {
            RandomGenerator generator = createGenerator(algorithm, seedValue, target);
            generator = applySplit(generator, split);
            return new RandomGeneratorAdapter(generator);
        }
    }

    private record GlobalState(boolean enabled, String algorithm, long seed, boolean hasSeed, StreamMode streamMode,
                               int split) {

        RandomSource create(Long seedFromCall, boolean forceThreadLocal) {
                long seedValue = resolveSeed(seedFromCall);
                boolean threadLocal = forceThreadLocal || streamMode == StreamMode.THREAD_LOCAL;
                if (threadLocal) {
                    return new TritiumThreadSafeRandomSource(algorithm, seedValue, split, TritiumRandomTarget.MINECRAFT_GLOBAL);
                }
                return new TritiumRandomSource(algorithm, seedValue, split, TritiumRandomTarget.MINECRAFT_GLOBAL);
            }

            private long resolveSeed(Long seedFromCall) {
                if (hasSeed) {
                    return seed;
                }
                if (seedFromCall != null) {
                    return seedFromCall;
                }
                return ThreadLocalRandom.current().nextLong();
            }
        }
}
