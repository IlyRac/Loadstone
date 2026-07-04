package com.ilyrac.loadstone.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record InteractionParticleS2CPacket(BlockPos pos, int ordinal) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("loadstone", "interaction_particle");

    public static final Type<InteractionParticleS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, InteractionParticleS2CPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeBlockPos(pkt.pos());
                        buf.writeInt(pkt.ordinal());
                    },
                    buf -> new InteractionParticleS2CPacket(
                            buf.readBlockPos(),
                            buf.readInt()
                    )
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}