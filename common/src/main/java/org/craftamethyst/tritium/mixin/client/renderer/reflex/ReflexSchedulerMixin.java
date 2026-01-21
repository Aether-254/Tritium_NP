package org.craftamethyst.tritium.mixin.client.renderer.reflex;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.packs.resources.ResourceManager;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL33C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class ReflexSchedulerMixin {

    @Unique
    private static final double NS_TO_SECONDS = 1e-9;
    @Unique
    private final long[] tritium$gpuSamples = new long[60];
    @Unique
    private long tritium$lastCpuTime;
    @Unique
    private long tritium$estCpuTime = -1;
    @Unique
    private long tritium$estGpuTime = -1;
    @Unique
    private int tritium$queryStart;
    @Unique
    private int tritium$queryEnd;
    @Unique
    private long tritium$gpuEndTime;
    @Unique
    private boolean tritium$queryActive;
    @Unique
    private int tritium$sampleIdx;
    @Unique
    private int tritium$sampleCnt;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reflex$init(Minecraft pMinecraft, ItemInHandRenderer pItemInHandRenderer,
                             ResourceManager pResourceManager, RenderBuffers pRenderBuffers, CallbackInfo ci) {
        GL.getCapabilities();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void reflex$frameStart(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        if (!TritiumConfigBase.Rendering.Reflex.enableReflex || !GL.getCapabilities().GL_ARB_timer_query) return;

        if (tritium$queryEnd != 0) {
            if (GL33C.glGetQueryObjecti64(tritium$queryEnd, GL33C.GL_QUERY_RESULT_AVAILABLE) == 1) {
                long endGpu = GL33C.glGetQueryObjecti64(tritium$queryEnd, GL33C.GL_QUERY_RESULT);
                if (tritium$queryStart != 0 && GL33C.glGetQueryObjecti64(tritium$queryStart, GL33C.GL_QUERY_RESULT_AVAILABLE) == 1) {
                    long startGpu = GL33C.glGetQueryObjecti64(tritium$queryStart, GL33C.GL_QUERY_RESULT);
                    long[] gpuNow = new long[1];
                    GL33C.glGetInteger64v(GL33C.GL_TIMESTAMP, gpuNow);
                    long sysNow = System.nanoTime();
                    long offset = sysNow - gpuNow[0];
                    long gpuTime = (endGpu + offset) - (startGpu + offset);

                    tritium$gpuSamples[tritium$sampleIdx] = gpuTime;
                    tritium$sampleIdx = (tritium$sampleIdx + 1) % 60;
                    if (tritium$sampleCnt < 60) tritium$sampleCnt++;

                    long sum = 0;
                    for (int i = 0; i < tritium$sampleCnt; i++) sum += tritium$gpuSamples[i];
                    tritium$estGpuTime = sum / tritium$sampleCnt;
                    tritium$gpuEndTime = endGpu + offset;

                    GL32C.glDeleteQueries(tritium$queryStart);
                    GL32C.glDeleteQueries(tritium$queryEnd);
                    tritium$queryStart = 0;
                    tritium$queryEnd = 0;
                    tritium$queryActive = false;
                }
            }
        }

        if (tritium$estCpuTime > 0 && tritium$estGpuTime > 0 && tritium$gpuEndTime > 0) {
            long now = System.nanoTime();
            long elapsed = now - tritium$gpuEndTime;
            if (elapsed < tritium$estGpuTime) {
                long wait = tritium$estGpuTime - elapsed - TritiumConfigBase.Rendering.Reflex.reflexOffsetNs;
                int maxFps = TritiumConfigBase.Rendering.Reflex.MAX_FPS;
                if (maxFps > 0) {
                    long minTime = 1000000000L / maxFps;
                    long frameElapsed = now - tritium$lastCpuTime;
                    long remaining = minTime - frameElapsed;
                    if (remaining > 0) wait = Math.max(wait, remaining);
                }
                if (wait > 1000000L) {
                    wait = Math.min(wait, 33000000L);
                    GLFW.glfwWaitEventsTimeout(wait * NS_TO_SECONDS);
                }
            }
        }

        tritium$lastCpuTime = System.nanoTime();

        if (!tritium$queryActive) {
            tritium$queryStart = GL32C.glGenQueries();
            GL33C.glQueryCounter(tritium$queryStart, GL33C.GL_TIMESTAMP);
            tritium$queryActive = true;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void reflex$frameEnd(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        if (!TritiumConfigBase.Rendering.Reflex.enableReflex || !GL.getCapabilities().GL_ARB_timer_query) return;

        if (tritium$queryActive && tritium$queryEnd == 0) {
            tritium$queryEnd = GL32C.glGenQueries();
            GL33C.glQueryCounter(tritium$queryEnd, GL33C.GL_TIMESTAMP);
        }

        long cpuTime = System.nanoTime() - tritium$lastCpuTime;
        if (tritium$estCpuTime < 0) {
            tritium$estCpuTime = cpuTime;
        } else {
            tritium$estCpuTime = (long) (0.85 * cpuTime + 0.15 * tritium$estCpuTime);
        }
    }
}