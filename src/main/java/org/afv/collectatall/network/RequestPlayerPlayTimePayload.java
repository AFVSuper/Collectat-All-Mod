package org.afv.collectatall.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.afv.collectatall.CollectatAll;

public record RequestPlayerPlayTimePayload() implements CustomPayload {
    public static final Id<RequestPlayerPlayTimePayload> ID =
        new Id<>(Identifier.of(CollectatAll.ModID, "request_play_time"));

    public static final PacketCodec<PacketByteBuf, RequestPlayerPlayTimePayload> CODEC =
            PacketCodec.of((buf, payload) -> {}, buf -> new RequestPlayerPlayTimePayload());
    
    public RequestPlayerPlayTimePayload(PacketByteBuf buf) {
        this();
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}