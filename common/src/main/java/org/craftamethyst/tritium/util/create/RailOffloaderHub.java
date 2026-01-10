package org.craftamethyst.tritium.util.create;

import com.simibubi.create.Create;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.platform.Services;

import java.util.concurrent.*;

public final class RailOffloaderHub {
    private static final boolean CREATE_LOADED = Services.PLATFORM.isModLoaded("create");
    private static SingleTaskLane worker;
    private static volatile Future<?> currentFuture;
    private static volatile boolean initialized = false;
    private static final Object LOCK = new Object();

    public static void initialize() {
        if (!CREATE_LOADED) return;

        synchronized (LOCK) {
            if (initialized) return;
            if (!TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading) {
                return;
            }

            worker = new SingleTaskLane("TritiumRailWorker");
            initialized = true;
            TritiumCommon.LOG.info("Tritium rail offloader initialized with ForkJoinPool");
        }
    }

    public static void shutdown() {
        if (!CREATE_LOADED) return;

        synchronized (LOCK) {
            initialized = false;
            if (worker != null) {
                worker.shutdown();
                worker = null;
            }
        }
    }

    public static void onTickStart(MinecraftServer server) {
        if (!CREATE_LOADED || !initialized || worker == null) return;
        if (!TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading) {
            return;
        }

        if (currentFuture != null && !currentFuture.isDone()) {
            TritiumCommon.LOG.warn("Previous rail tick still running, skipping this tick");
            return;
        }

        ServerLevel overworld = server.overworld();
        currentFuture = worker.submit(() -> {
            try {
                Create.RAILWAYS.tick(overworld);
            } catch (Exception e) {
                TritiumCommon.LOG.error("Error in rail offloader tick", e);
            }
        });
    }

    public static void onTickEnd() {
        if (!CREATE_LOADED || currentFuture == null) return;

        try {
            currentFuture.get(50, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            TritiumCommon.LOG.warn("Rail tick took too long, will continue asynchronously");
        } catch (Exception e) {
            if (!(e instanceof CancellationException)) {
                TritiumCommon.LOG.error("Error waiting for rail tick", e);
            }
        } finally {
            if (currentFuture != null && currentFuture.isDone()) {
                currentFuture = null;
            }
        }
    }
}