package org.afv.collectatall.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.afv.collectatall.CollectatAll;

public record CollectatallModePayload(String mode, int minutes, int count) implements CustomPayload {
    public static final Id<CollectatallModePayload> ID =
        new Id<>(Identifier.of(CollectatAll.ModID, "mode_payload"));

    public static final PacketCodec<PacketByteBuf, CollectatallModePayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> {
                        buf.writeString(payload.mode());
                        buf.writeInt(payload.minutes());
                        buf.writeInt(payload.count());
                    },
                    buf -> new CollectatallModePayload(buf.readString(), buf.readInt(), buf.readInt())
            );

    public CollectatallModePayload(PacketByteBuf buf) {
        this(buf.readString(), buf.readInt(), buf.readInt());
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}