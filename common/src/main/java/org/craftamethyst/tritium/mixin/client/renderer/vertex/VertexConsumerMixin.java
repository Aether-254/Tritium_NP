package org.craftamethyst.tritium.mixin.client.renderer.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {

    /**
     * @author ZCRAFT
     * Zero-allocation vertex transformation with manual matrix expansion
     */
    @Overwrite
    default VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        final float m00 = matrix.m00(), m10 = matrix.m10(), m20 = matrix.m20(), m30 = matrix.m30();
        final float m01 = matrix.m01(), m11 = matrix.m11(), m21 = matrix.m21(), m31 = matrix.m31();
        final float m02 = matrix.m02(), m12 = matrix.m12(), m22 = matrix.m22(), m32 = matrix.m32();

        final float xt = m00 * x + m10 * y + m20 * z + m30;
        final float yt = m01 * x + m11 * y + m21 * z + m31;
        final float zt = m02 * x + m12 * y + m22 * z + m32;

        return ((VertexConsumer) this).vertex(xt, yt, zt);
    }
}