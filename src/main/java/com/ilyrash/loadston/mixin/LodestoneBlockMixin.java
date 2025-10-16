package com.ilyrash.loadston.mixin;

import com.ilyrash.loadston.LoadStone;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class LodestoneBlockMixin {

    @Shadow
    protected World world;

    @Final
    @Shadow
    protected BlockPos pos;

    @Inject(method = "markRemoved", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        BlockState state = self.getCachedState();

        // Only process lodestones that are active chunk loaders
        if (state.isOf(Blocks.LODESTONE) && world != null && !world.isClient()) {
            if (LoadStone.isChunkLoaderActive(pos)) {
                LoadStone.deactivateChunkLoader(world, pos);
            }
        }
    }
}