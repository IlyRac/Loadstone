package com.ilyrac.loadstone;

import com.ilyrac.loadstone.loader.LoaderTier;
import net.minecraft.core.BlockPos;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientLoaderCache {

    private static final Map<BlockPos, LoaderTier> CACHE = new HashMap<>();

    private ClientLoaderCache() {}

    public static Optional<LoaderTier> get(BlockPos pos) {
        return Optional.ofNullable(CACHE.get(pos));
    }

    public static void put(BlockPos pos, LoaderTier tier) {
        CACHE.put(pos.immutable(), tier);
    }

    public static void remove(BlockPos pos) {
        CACHE.remove(pos);
    }

    public static void clear() {
        CACHE.clear();
    }

    public static Map<BlockPos, LoaderTier> snapshot() {
        return Map.copyOf(CACHE);
    }

    public static boolean isChunkLoaded(int chunkX, int chunkZ) {
        for (Map.Entry<BlockPos, LoaderTier> entry : CACHE.entrySet()) {
            BlockPos bp = entry.getKey();
            LoaderTier tier = entry.getValue();
            int dx = Math.abs(chunkX - (bp.getX() >> 4));
            int dz = Math.abs(chunkZ - (bp.getZ() >> 4));
            if (dx <= tier.getRadius() && dz <= tier.getRadius()) {
                return true;
            }
        }
        return false;
    }
}

