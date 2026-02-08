package org.craftamethyst.tritium.mixin.jigsaw;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureBlockEntityMixin {

    @Inject(
            method = "processBlockInfos(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void tritium$filterBlocks(
            ServerLevelAccessor level,
            BlockPos offset,
            BlockPos pos,
            StructurePlaceSettings settings,
            List<StructureBlockInfo> original,
            CallbackInfoReturnable<List<StructureBlockInfo>> cir) {

        if (!TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations ||
                !TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableStructureBlockFiltering) {
            return;
        }

        List<StructureBlockInfo> result = cir.getReturnValue();
        if (result == null || result.isEmpty()) {
            return;
        }

        BoundingBox box = settings.getBoundingBox();
        if (box == null) {
            return;
        }

        List<StructureBlockInfo> filtered = new ArrayList<>(result.size());
        for (StructureBlockInfo info : result) {
            BlockPos relativePos = StructureTemplate.calculateRelativePosition(settings, info.pos());
            BlockPos worldPos = relativePos.offset(offset);

            if (box.isInside(worldPos)) {
                filtered.add(info);
            }
        }

        if (filtered.isEmpty() && !result.isEmpty()) {
            return;
        }

        cir.setReturnValue(filtered);
    }
}