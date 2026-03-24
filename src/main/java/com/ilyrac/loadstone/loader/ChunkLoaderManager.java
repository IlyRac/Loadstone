package com.ilyrac.loadstone.loader;

import com.ilyrac.loadstone.network.ServerNetworking;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkLoaderManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static LoadstoneData getState(ServerLevel world) {
        return LoadstoneData.getLevelState(world);
    }

    public static boolean isActive(ServerLevel world, BlockPos pos) {
        return getState(world).activeLoaders.containsKey(pos);
    }

    public static LoaderTier getLoader(ServerLevel world, BlockPos pos) {
        return getState(world).activeLoaders.get(pos);
    }

    public static boolean canActivate(ServerLevel world, BlockPos pos, LoaderTier tier) {
        LoadstoneData state = getState(world);
        if (state.activeLoaders.isEmpty()) return true;

        // Use ChunkPos.containing(pos) for cleaner code
        ChunkPos centerNew = ChunkPos.containing(pos);
        int rNew = tier.getRadius();

        for (Map.Entry<BlockPos, LoaderTier> entry : state.activeLoaders.entrySet()) {
            BlockPos existingPos = entry.getKey();
            LoaderTier existingTier = entry.getValue();

            if (existingPos == null || existingTier == null) continue;

            ChunkPos centerExisting = ChunkPos.containing(existingPos);
            int rExisting = existingTier.getRadius();

            // Access x and z via accessor methods x() and z()
            int dx = Math.abs(centerNew.x() - centerExisting.x());
            int dz = Math.abs(centerNew.z() - centerExisting.z());

            if (dx <= (rNew + rExisting) && dz <= (rNew + rExisting)) {
                return false;
            }
        }
        return true;
    }

    public static void activate(ServerLevel world, BlockPos pos, LoaderTier tier) {
        if (tier == null) return;

        int radius = tier.getRadius();
        ChunkPos centerChunk = ChunkPos.containing(pos);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Accessors: .x() and .z()
                addChunkTicket(world, new ChunkPos(centerChunk.x() + x, centerChunk.z() + z));
            }
        }
        LoadstoneData state = getState(world);
        state.activeLoaders.put(pos, tier);
        state.setDirty();
        ServerNetworking.broadcastUpdate(world, pos, tier);
    }

    public static void deactivate(ServerLevel world, BlockPos pos) {
        LoadstoneData state = getState(world);
        LoaderTier tier = state.activeLoaders.remove(pos);
        if (tier == null) return;

        state.setDirty();

        try {
            ItemStack dropStack = new ItemStack(tier.getActivatorItem(), 1);
            ItemEntity entity = new ItemEntity(world,
                    pos.getX() + 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5,
                    dropStack);
            world.addFreshEntity(entity);
        } catch (Throwable t) {
            LOGGER.error("Failed to drop activator item at {}", pos, t);
        }

        int radius = tier.getRadius();
        ChunkPos centerChunk = ChunkPos.containing(pos);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                removeChunkTicket(world, new ChunkPos(centerChunk.x() + x, centerChunk.z() + z));
            }
        }

        ServerNetworking.broadcastUpdate(world, pos, null);
    }

    private static void addChunkTicket(ServerLevel world, ChunkPos chunkPos) {
        world.setChunkForced(chunkPos.x(), chunkPos.z(), true);
        LevelChunk chunk = world.getChunk(chunkPos.x(), chunkPos.z());
        chunk.markUnsaved();
    }

    private static void removeChunkTicket(ServerLevel world, ChunkPos chunkPos) {
        world.setChunkForced(chunkPos.x(), chunkPos.z(), false);
    }

    public static Map<BlockPos, LoaderTier> snapshot(ServerLevel world) {
        return new HashMap<>(getState(world).activeLoaders);
    }

    public static void validateAllLoaders(ServerLevel level) {
        LoadstoneData state = getState(level);
        if (state.activeLoaders.isEmpty()) return;
        List<BlockPos> toRemove = new ArrayList<>();

        for (BlockPos pos : state.activeLoaders.keySet()) {
            if (!level.isLoaded(pos)) continue;
            if (!level.getBlockState(pos).is(Blocks.LODESTONE)) {
                toRemove.add(pos);
            }
        }

        for (BlockPos pos : toRemove) {
            deactivate(level, pos);
        }
    }
}