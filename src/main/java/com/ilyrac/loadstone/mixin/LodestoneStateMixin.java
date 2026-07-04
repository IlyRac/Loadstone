package com.ilyrac.loadstone.mixin;

import com.ilyrac.loadstone.loader.ChunkLoaderManager;
import com.ilyrac.loadstone.loader.LoaderTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class LodestoneStateMixin {

    @SuppressWarnings("all")
    @Inject(method = "hasAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
    private void loadstoneHasSignal(CallbackInfoReturnable<Boolean> cir) {
        BlockState state = (BlockState) (Object) this;
        if (state.is(Blocks.LODESTONE)) {
            cir.setReturnValue(true);
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "getAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
    private void loadstoneGetSignal(Level level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        BlockState state = (BlockState) (Object) this;

        if (state.is(Blocks.LODESTONE) && level instanceof ServerLevel serverLevel) {
            if (ChunkLoaderManager.isActive(serverLevel, pos)) {
                LoaderTier tier = ChunkLoaderManager.getLoader(serverLevel, pos);

                if (tier != null) {
                    int signalStrength = switch (tier) {
                        case IRON -> 5;
                        case DIAMOND -> 10;
                        case NETHERITE -> 15;
                    };
                    cir.setReturnValue(signalStrength);
                    return;
                }
            }
            cir.setReturnValue(0);
        }
    }
}