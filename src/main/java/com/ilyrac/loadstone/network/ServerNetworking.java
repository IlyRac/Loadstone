package com.ilyrac.loadstone.network;

import com.ilyrac.loadstone.loader.ChunkLoaderManager;
import com.ilyrac.loadstone.loader.LoaderTier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ServerNetworking {

    private ServerNetworking() {}

    // Call this from Loadstone.onInitialize()
    public static void Initializer() {
        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
            ServerPlayer player = handler.getPlayer();
            sendSnapshotTo(player);
        });
    }

    // Broadcast a single update to players tracking the pos
    public static void broadcastUpdate(ServerLevel world, BlockPos pos, LoaderTier tier) {
        for (ServerPlayer player : PlayerLookup.tracking(world, pos)) {
            LoaderUpdateS2CPacket payload;
            if (tier != null) payload = new LoaderUpdateS2CPacket(pos, true, tier.ordinal());
            else payload = new LoaderUpdateS2CPacket(pos, false, 0);

            ServerPlayNetworking.send(player, payload);
        }
    }

    // Send full snapshot to a single player
    public static void sendSnapshotTo(ServerPlayer player) {
        Map<BlockPos, LoaderTier> snapshot = ChunkLoaderManager.snapshot(player.level());
        List<LoaderUpdateS2CPacket> entries = snapshot.entrySet().stream()
                .map(e -> new LoaderUpdateS2CPacket(e.getKey(), true, e.getValue().ordinal()))
                .collect(Collectors.toList());

        LoaderSnapshotS2CPacket payload = new LoaderSnapshotS2CPacket(entries);
        ServerPlayNetworking.send(player, payload);
    }
}