package org.craftamethyst.tritium.cull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

public class LeafCullingContext {
    private static final ThreadLocal<RenderContext> CURRENT_CONTEXT = ThreadLocal.withInitial(() -> null);

    public static RenderContext getCurrentRenderContext() {
        return CURRENT_CONTEXT.get();
    }

    public static void setCurrentRenderContext(RenderContext context) {
        CURRENT_CONTEXT.set(context);
    }

    public static void clearCurrentRenderContext() {
        CURRENT_CONTEXT.remove();
    }

    public static boolean shouldHideInnerLeaves(BlockGetter level, BlockPos pos) {
        return LeafCulling.shouldHideInnerLeaves(level, pos);
    }

    public static boolean shouldCullBlockFace(BlockGetter level, BlockPos pos, Direction face) {
        return BlockFaceOcclusionCuller.shouldCullBlockFace(level, pos, face);
    }

    public static boolean shouldCullFace(BlockGetter level, BlockPos pos, Direction face) {
        return LeafCulling.shouldCullFace(level, pos, face);
    }

    public record RenderContext(BlockPos currentPos, BlockGetter level) {
    }
}