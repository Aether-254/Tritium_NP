package org.craftamethyst.tritium.cull;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public final class BlockFaceOcclusionCuller {
    private static final AtomicBoolean FALLBACK_MODE = new AtomicBoolean(false);
    private static final AtomicBoolean RECOVERY_MODE = new AtomicBoolean(false);

    private static final int TRACE_DISTANCE = 16;
    private static final double SAMPLE_OFFSET = 0.2;

    private static final Cache<Key, Boolean> SHORT_TERM_CACHE = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(500, TimeUnit.MILLISECONDS)
            .build();

    private static final Cache<Key, Boolean> LONG_TERM_CACHE = Caffeine.newBuilder()
            .maximumSize(40000)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    private static final LongAdder PENDING = new LongAdder();
    private static final ConcurrentMap<Key, CompletableFuture<Boolean>> INFLIGHT = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Key>> DEPENDENCY_MAP = new ConcurrentHashMap<>();

    private static volatile boolean POOLS_INITIALIZED = false;
    private static volatile ExecutorService tracerPool;
    private static volatile ScheduledExecutorService timeoutChecker;
    private static long lastFallbackCheck = System.currentTimeMillis();

    public static boolean shouldCullBlockFace(BlockGetter level, BlockPos pos, Direction face) {
        if (FALLBACK_MODE.get()) {
            if (System.currentTimeMillis() - lastFallbackCheck > 10000) {
                FALLBACK_MODE.compareAndSet(true, false);
                lastFallbackCheck = System.currentTimeMillis();
            }
            return LeafCulling.checkSimpleConnection(level, pos.relative(face));
        }

        final Key key = createKey(level, pos, face);

        Boolean shortCached = SHORT_TERM_CACHE.getIfPresent(key);
        if (shortCached != null) {
            return shortCached;
        }

        Boolean longCached = LONG_TERM_CACHE.getIfPresent(key);
        if (longCached != null) {
            SHORT_TERM_CACHE.put(key, longCached);
            return longCached;
        }

        final BlockPos adjacentPos = pos.relative(face);
        final BlockState neighbor = level.getBlockState(adjacentPos);

        if (neighbor.isAir()) {
            cacheResult(key, false);
            return false;
        }

        if (neighbor.isFaceSturdy(level, adjacentPos, face.getOpposite()) ||
                LeafCulling.checkSimpleConnection(level, adjacentPos)) {
            cacheResult(key, true);
            return true;
        }

        if (PENDING.sum() > 2048) {
            cacheResult(key, false);
            return false;
        }

        return scheduleTrace(level, pos, face, key);
    }

    private static Key createKey(BlockGetter level, BlockPos pos, Direction face) {
        long neighborHash = calculateNeighborHash(level, pos, face);
        return new Key(level.hashCode(), pos.asLong(), (byte) face.ordinal(), neighborHash);
    }

    private static long calculateNeighborHash(BlockGetter level, BlockPos pos, Direction face) {
        long hash = 0;
        BlockPos checkPos = pos.relative(face);

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = checkPos.relative(dir);
            BlockState state = level.getBlockState(neighbor);

            hash = 31 * hash + System.identityHashCode(state.getBlock());
            hash = 31 * hash + (state.isAir() ? 1 : 0);
            hash = 31 * hash + (state.isSolidRender() ? 1 : 0);
            hash = 31 * hash + state.getLightEmission();
        }
        return hash;
    }

    private static void cacheResult(Key key, boolean result) {
        SHORT_TERM_CACHE.put(key, result);
        LONG_TERM_CACHE.put(key, result);
    }

    private static boolean scheduleTrace(BlockGetter level, BlockPos pos, Direction face, Key key) {
        CompletableFuture<Boolean> future = INFLIGHT.get(key);
        if (future != null && !future.isDone()) {
            return false;
        }

        final BlockGetter levelFinal = level;
        final BlockPos posFinal = pos;
        final Direction faceFinal = face;

        future = new CompletableFuture<>();
        CompletableFuture<Boolean> existing = INFLIGHT.putIfAbsent(key, future);
        if (existing != null) {
            return false;
        }

        PENDING.increment();
        initExecutors();
        recordDependencies(level, pos, face, key);

        CompletableFuture<Boolean> finalFuture = future;
        Runnable work = () -> {
            try {
                if (Thread.currentThread().isInterrupted()) {
                    finalFuture.complete(false);
                    return;
                }

                Vec3 startCenter = getFaceCenter(posFinal, faceFinal);
                Vec3 dir = new Vec3(faceFinal.getStepX(), faceFinal.getStepY(), faceFinal.getStepZ());
                Vec3 endCenter = startCenter.add(dir.scale(TRACE_DISTANCE));

                int sampleCount = calculateSampleCount(posFinal, levelFinal);
                boolean anyVisible = traceVisibilityMultiSample(startCenter, endCenter, levelFinal, faceFinal, sampleCount);
                boolean shouldCull = !anyVisible;

                finalFuture.complete(shouldCull);
            } catch (Throwable t) {
                finalFuture.completeExceptionally(t);
            } finally {
                PENDING.decrement();
            }
        };

        final CompletableFuture<Boolean> taskFuture = future;
        Future<?> task = tracerPool.submit(work);
        timeoutChecker.schedule(() -> {
            if (!taskFuture.isDone()) {
                task.cancel(true);
                taskFuture.complete(false);
                if (PENDING.sum() > 1024) {
                    FALLBACK_MODE.set(true);
                }
            }
        }, 10, TimeUnit.MILLISECONDS);

        taskFuture.whenComplete((res, err) -> {
            try {
                if (err != null) {
                    if (!RECOVERY_MODE.get()) {
                        FALLBACK_MODE.set(true);
                        RECOVERY_MODE.set(true);
                    }
                    boolean fb = LeafCulling.checkSimpleConnection(levelFinal, posFinal.relative(faceFinal));
                    cacheResult(key, fb);
                } else {
                    cacheResult(key, res);
                    if (RECOVERY_MODE.get() && PENDING.sum() < 512 && !FALLBACK_MODE.get()) {
                        RECOVERY_MODE.set(false);
                    }
                }
            } finally {
                INFLIGHT.remove(key);
            }
        });

        return false;
    }

    private static void recordDependencies(BlockGetter level, BlockPos pos, Direction face, Key key) {
        BlockPos checkPos = pos.relative(face);

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = checkPos.relative(dir);
            long neighborKey = neighborPos.asLong();

            DEPENDENCY_MAP.computeIfAbsent(neighborKey, k -> ConcurrentHashMap.newKeySet())
                    .add(key);
        }
    }

    public static void onBlockChanged(BlockPos pos) {
        long posKey = pos.asLong();
        Set<Key> dependentKeys = DEPENDENCY_MAP.remove(posKey);
        if (dependentKeys != null) {
            for (Key key : dependentKeys) {
                SHORT_TERM_CACHE.invalidate(key);
                LONG_TERM_CACHE.invalidate(key);
            }
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            for (Direction face : Direction.values()) {
                if (face.getOpposite() == dir) continue;

                Key neighborKey = createKeyFromPos(pos, face);
                SHORT_TERM_CACHE.invalidate(neighborKey);
                LONG_TERM_CACHE.invalidate(neighborKey);

                addDependency(neighborPos, pos);
            }
        }

        SHORT_TERM_CACHE.cleanUp();
        LONG_TERM_CACHE.cleanUp();
    }

    private static Key createKeyFromPos(BlockPos pos, Direction face) {
        return new Key(0, pos.asLong(), (byte) face.ordinal(), 0);
    }

    private static void addDependency(BlockPos dependentPos, BlockPos dependencyPos) {
        long depKey = dependencyPos.asLong();
        for (Direction face : Direction.values()) {
            Key key = createKeyFromPos(dependentPos, face);
            DEPENDENCY_MAP.computeIfAbsent(depKey, k -> ConcurrentHashMap.newKeySet())
                    .add(key);
        }
    }

    private static int calculateSampleCount(BlockPos pos, BlockGetter level) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return 3;

        int leafCount = 0;
        for (Direction dir : Direction.values()) {
            if (LeafCulling.checkSimpleConnection(level, pos.relative(dir))) {
                leafCount++;
            }
        }

        if (leafCount >= 5) return 7;
        if (leafCount >= 3) return 5;
        return 3;
    }

    private static boolean traceVisibilityMultiSample(Vec3 centerStart, Vec3 centerEnd, BlockGetter level, Direction face, int sampleCount) {
        Vec3[] offsets = sampleOffsets(face, sampleCount);
        for (Vec3 off : offsets) {
            if (Thread.currentThread().isInterrupted()) {
                return true;
            }
            Vec3 s = centerStart.add(off);
            Vec3 e = centerEnd.add(off);
            if (traceVisibility(s, e, level)) {
                return true;
            }
        }
        return false;
    }

    private static boolean traceVisibility(Vec3 start, Vec3 end, BlockGetter level) {
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        if (distance < 1.0e-3) return true;

        direction = direction.normalize();
        double stepSize = calculateStepSize(distance, level);
        int maxSteps = (int) Math.min(512, Math.ceil(distance / stepSize) + 4);

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        Vec3 current = start;
        int steps = 0;

        while (steps++ < maxSteps && current.distanceTo(start) < distance) {
            if (Thread.currentThread().isInterrupted()) {
                return true;
            }

            mpos.set(current.x, current.y, current.z);
            BlockState state = level.getBlockState(mpos);

            if (!state.isAir()) {
                if (!state.getOcclusionShape().isEmpty() &&
                        state.getCollisionShape(level, mpos).bounds().move(mpos).contains(current)) {
                    return false;
                }
            }

            current = current.add(direction.scale(stepSize));
        }
        return true;
    }

    private static double calculateStepSize(double distance, BlockGetter level) {
        if (distance > 8.0) return 0.5;
        if (distance > 4.0) return 0.25;
        if (distance > 2.0) return 0.125;
        if (level != null && level.getBlockState(BlockPos.containing(0, 0, 0)).isSolidRender()) {
            return 0.125;
        }
        return 0.0625;
    }

    private static Vec3[] sampleOffsets(Direction face, int count) {
        if (count <= 3) {
            return new Vec3[]{Vec3.ZERO};
        }

        Vec3[] offsets = new Vec3[count];
        offsets[0] = Vec3.ZERO;

        double offset = SAMPLE_OFFSET;
        if (count >= 7) offset = SAMPLE_OFFSET * 1.5;

        if (face == Direction.UP || face == Direction.DOWN) {
            offsets[1] = new Vec3(+offset, 0, 0);
            offsets[2] = new Vec3(-offset, 0, 0);
            if (count >= 5) {
                offsets[3] = new Vec3(0, 0, +offset);
                offsets[4] = new Vec3(0, 0, -offset);
            }
            if (count >= 7) {
                offsets[5] = new Vec3(+offset, 0, +offset);
                offsets[6] = new Vec3(-offset, 0, -offset);
            }
        } else if (face == Direction.NORTH || face == Direction.SOUTH) {
            offsets[1] = new Vec3(+offset, 0, 0);
            offsets[2] = new Vec3(-offset, 0, 0);
            if (count >= 5) {
                offsets[3] = new Vec3(0, +offset, 0);
                offsets[4] = new Vec3(0, -offset, 0);
            }
            if (count >= 7) {
                offsets[5] = new Vec3(+offset, +offset, 0);
                offsets[6] = new Vec3(-offset, -offset, 0);
            }
        } else if (face == Direction.EAST || face == Direction.WEST) {
            offsets[1] = new Vec3(0, +offset, 0);
            offsets[2] = new Vec3(0, -offset, 0);
            if (count >= 5) {
                offsets[3] = new Vec3(0, 0, +offset);
                offsets[4] = new Vec3(0, 0, -offset);
            }
            if (count >= 7) {
                offsets[5] = new Vec3(0, +offset, +offset);
                offsets[6] = new Vec3(0, -offset, -offset);
            }
        }

        return offsets;
    }

    private static Vec3 getFaceCenter(BlockPos pos, Direction face) {
        return new Vec3(
                pos.getX() + 0.5 + face.getStepX() * 0.501,
                pos.getY() + 0.5 + face.getStepY() * 0.501,
                pos.getZ() + 0.5 + face.getStepZ() * 0.501
        );
    }

    public static boolean isInFallbackMode() {
        return FALLBACK_MODE.get();
    }

    public static void shutdown() {
        if (tracerPool != null) {
            tracerPool.shutdownNow();
            tracerPool = null;
        }
        if (timeoutChecker != null) {
            timeoutChecker.shutdownNow();
            timeoutChecker = null;
        }
        SHORT_TERM_CACHE.invalidateAll();
        LONG_TERM_CACHE.invalidateAll();
        INFLIGHT.clear();
        DEPENDENCY_MAP.clear();
        PENDING.reset();
        FALLBACK_MODE.set(false);
        RECOVERY_MODE.set(false);
        POOLS_INITIALIZED = false;
    }

    private static synchronized void initExecutors() {
        if (POOLS_INITIALIZED && tracerPool != null && !tracerPool.isShutdown() &&
                timeoutChecker != null && !timeoutChecker.isShutdown()) {
            return;
        }

        int cpus = Math.max(1, Runtime.getRuntime().availableProcessors());
        tracerPool = new ThreadPoolExecutor(
                Math.max(1, cpus / 4),
                Math.max(2, cpus / 2),
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(4096),
                new NamedThreadFactory("Tritium-occl-trace", true),
                new ThreadPoolExecutor.DiscardPolicy()
        );
        timeoutChecker = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Tritium-occl-timeout", true));
        POOLS_INITIALIZED = true;
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final String baseName;
        private final boolean daemon;
        private final AtomicInteger idx = new AtomicInteger(1);

        private NamedThreadFactory(String baseName, boolean daemon) {
            this.baseName = Objects.requireNonNull(baseName);
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r, baseName + "-" + idx.getAndIncrement());
            t.setDaemon(daemon);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        }
    }

    private record Key(int levelId, long pos, byte face, long neighborHash) {
    }
}