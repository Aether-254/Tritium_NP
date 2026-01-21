package org.craftamethyst.tritium.mixin.create.renderbounds;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.tritium.util.create.CreateRenderBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChainConveyorBlockEntity.class)
public abstract class ChainConveyorBlockEntityRenderBoundsMixin {
    @Shadow
    public Set<BlockPos> connections;

    @Inject(method = "createRenderBoundingBox", at = @At("RETURN"), cancellable = true, remap = false)
    private void tritium$clampRenderBounds(CallbackInfoReturnable<AABB> cir) {
        if (connections == null || connections.isEmpty()) {
            return;
        }
        BlockPos pos = ((BlockEntity) (Object) this).getBlockPos();
        AABB bounds = CreateRenderBounds.chainConveyorBounds(pos, connections, 1.0);
        cir.setReturnValue(bounds);
    }
}
