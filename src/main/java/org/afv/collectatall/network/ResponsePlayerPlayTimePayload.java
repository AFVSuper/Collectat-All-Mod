package org.afv.collectatall.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.afv.collectatall.CollectatAll;

public record ResponsePlayerPlayTimePayload(long count) implements CustomPayload {
    public static final Id<ResponsePlayerPlayTimePayload> ID =
        new Id<>(Identifier.of(CollectatAll.ModID, "play_time_response"));

    public static final PacketCodec<PacketByteBuf, ResponsePlayerPlayTimePayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> buf.writeLong(payload.count()),
                    buf -> new ResponsePlayerPlayTimePayload(buf.readLong())
            );
    
    public ResponsePlayerPlayTimePayload(PacketByteBuf buf) {
        this(buf.readInt());
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}