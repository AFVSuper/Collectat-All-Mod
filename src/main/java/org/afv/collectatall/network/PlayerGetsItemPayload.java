package org.afv.collectatall.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.afv.collectatall.CollectatAll;

public record PlayerGetsItemPayload() implements CustomPayload {
    public static final Id<PlayerGetsItemPayload> ID =
        new Id<>(Identifier.of(CollectatAll.ModID, "player_gets_item_packet"));

    public static final PacketCodec<PacketByteBuf, PlayerGetsItemPayload> CODEC =
            PacketCodec.of((buf, payload) -> {}, buf -> new PlayerGetsItemPayload());

    public PlayerGetsItemPayload(PacketByteBuf buf) {
        this();
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}