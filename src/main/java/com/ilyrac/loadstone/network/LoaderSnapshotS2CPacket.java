package com.ilyrac.loadstone.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record LoaderSnapshotS2CPacket(List<LoaderUpdateS2CPacket> entries) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("loadstone", "loader_snapshot");

    public static final CustomPacketPayload.Type<LoaderSnapshotS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    // Simple StreamCodec: write count then each entry as (pos,bool,int)
    public static final StreamCodec<RegistryFriendlyByteBuf, LoaderSnapshotS2CPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeInt(pkt.entries().size());
                        for (LoaderUpdateS2CPacket e : pkt.entries()) {
                            buf.writeBlockPos(e.pos());
                            buf.writeBoolean(e.hasLoader());
                            buf.writeInt(e.ordinal());
                        }
                    },
                    buf -> {
                        int count = buf.readInt();
                        List<LoaderUpdateS2CPacket> list = new ArrayList<>(Math.max(4, count));
                        for (int i = 0; i < count; i++) {
                            BlockPos pos = buf.readBlockPos();
                            boolean has = buf.readBoolean();
                            int ord = buf.readInt();
                            list.add(new LoaderUpdateS2CPacket(pos, has, ord));
                        }
                        return new LoaderSnapshotS2CPacket(list);
                    }
            );

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}