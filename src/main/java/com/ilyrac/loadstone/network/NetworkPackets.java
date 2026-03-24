package com.ilyrac.loadstone.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class NetworkPackets {
    private NetworkPackets() {}

    public static void Initializer() {
        PayloadTypeRegistry.clientboundPlay().register(LoaderUpdateS2CPacket.TYPE, LoaderUpdateS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LoaderSnapshotS2CPacket.TYPE, LoaderSnapshotS2CPacket.STREAM_CODEC);
    }
}