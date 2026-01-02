package org.afv.collectatall.network;

import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.afv.collectatall.CollectatAll;

public record PlayerRemainingCountPayload(int count, String item) implements CustomPayload {
    public static final Id<PlayerRemainingCountPayload> ID =
        new Id<>(Identifier.of(CollectatAll.ModID, "player_remaining_count"));

    public static final PacketCodec<PacketByteBuf, PlayerRemainingCountPayload> CODEC =
            PacketCodec.of( (payload, buf) -> {
                        buf.writeInt(payload.count());
                        if (payload.item() == null) buf.writeString("minecraft:air");
                        else buf.writeString(payload.item());
                    },
                    buf -> new PlayerRemainingCountPayload(buf.readInt(), buf.readString()));

    public PlayerRemainingCountPayload(PacketByteBuf buf) {
        this(buf.readInt(), buf.readString());
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}