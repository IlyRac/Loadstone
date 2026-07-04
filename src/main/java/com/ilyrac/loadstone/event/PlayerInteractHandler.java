package com.ilyrac.loadstone.event;

import com.ilyrac.loadstone.loader.ChunkLoaderManager;
import com.ilyrac.loadstone.loader.LoaderTier;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerInteractHandler {

    public static void Initializer() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (!state.is(Blocks.LODESTONE)) return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);
            LoaderTier tier = LoaderTier.fromItem(held.getItem());

            if (ChunkLoaderManager.isActive((ServerLevel) world, pos)) {
                LoaderTier current = ChunkLoaderManager.getLoader((ServerLevel) world, pos);

                if (tier != null && tier != current) {
                    ChunkLoaderManager.deactivate((ServerLevel) world, pos);
                    if (!player.isCreative()) held.shrink(1);
                    ChunkLoaderManager.activate((ServerLevel) world, pos, tier);
                    playServerSounds((ServerLevel) world, pos, tier);

                    return InteractionResult.SUCCESS;
                } else if (held.isEmpty()) {
                    ChunkLoaderManager.deactivate((ServerLevel) world, pos);
                    playServerSounds((ServerLevel) world, pos, current);

                    player.swing(hand, true);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            } else {
                if (tier != null) {
                    ServerLevel serverLevel = (ServerLevel) world;

                    if (ChunkLoaderManager.canActivate(serverLevel, pos, tier)) {
                        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                            serverPlayer.sendOverlayMessage(
                                    Component.literal("Cannot activate loader here: would overlap an existing loader.")
                            );
                        }

                        player.swing(hand, true);
                        return InteractionResult.FAIL;
                    }

                    ChunkLoaderManager.activate(serverLevel, pos, tier);
                    if (!player.isCreative()) held.shrink(1);
                    playServerSounds(serverLevel, pos, tier);

                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.PASS;
                }
            }
        });
    }

    // Notice we removed particles entirely from the server!
    private static void playServerSounds(ServerLevel world, BlockPos pos, LoaderTier tier) {
        switch (tier) {
            case IRON -> world.playSound(null, pos, SoundEvents.IRON_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            case DIAMOND -> world.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            case NETHERITE -> world.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }
}