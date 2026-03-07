package com.ilyrac.loadstone.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record LoaderUpdateS2CPacket(BlockPos pos, boolean hasLoader, int ordinal) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("loadstone", "loader_update");

    // The Type generic mirrors your Terracart style
    public static final CustomPacketPayload.Type<LoaderUpdateS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    // Stream codec: write/read pos, boolean, int
    public static final StreamCodec<RegistryFriendlyByteBuf, LoaderUpdateS2CPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeBlockPos(pkt.pos());
                        buf.writeBoolean(pkt.hasLoader());
                        buf.writeInt(pkt.ordinal());
                    },
                    buf -> new LoaderUpdateS2CPacket(
                            buf.readBlockPos(),
                            buf.readBoolean(),
                            buf.readInt()
                    )
            );

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}