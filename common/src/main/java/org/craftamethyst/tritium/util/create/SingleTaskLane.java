package org.craftamethyst.tritium.util.create;

import org.craftamethyst.tritium.TritiumCommon;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class SingleTaskLane {
    private final ForkJoinPool workerPool;
    private final AtomicBoolean running = new AtomicBoolean(true);

    SingleTaskLane(String name) {
        workerPool = new ForkJoinPool(
                1, // 只需要一个核心线程
                pool -> {
                    ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    thread.setName(name);
                    thread.setDaemon(true);
                    thread.setPriority(Thread.NORM_PRIORITY + 1);
                    return thread;
                },
                (t, e) -> TritiumCommon.LOG.error("Rail worker thread error", e),
                true // 异步模式
        );
    }

    Future<?> submit(Runnable r) {
        if (!running.get()) return CompletableFuture.completedFuture(null);

        try {
            return workerPool.submit(r);
        } catch (RejectedExecutionException e) {
            TritiumCommon.LOG.warn("Failed to submit rail task, pool is shutting down");
            return CompletableFuture.completedFuture(null);
        }
    }

    void shutdown() {
        running.set(false);
        if (workerPool != null && !workerPool.isShutdown()) {
            workerPool.shutdownNow();
            try {
                if (!workerPool.awaitTermination(2, TimeUnit.SECONDS)) {
                    TritiumCommon.LOG.warn("Rail worker pool did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}