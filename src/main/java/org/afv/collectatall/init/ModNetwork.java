package org.afv.collectatall.init;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.afv.collectatall.network.*;
import org.afv.collectatall.util.CollectatallMode;
import org.afv.collectatall.util.CollectatallModeState;

public class ModNetwork {
    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(
                RequestPlayerPlayTimePayload.ID,
                RequestPlayerPlayTimePayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                ResponsePlayerPlayTimePayload.ID,
                ResponsePlayerPlayTimePayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                PlayerGetsItemPayload.ID,
                PlayerGetsItemPayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                PlayerRemainingCountPayload.ID,
                PlayerRemainingCountPayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                CollectatallModePayload.ID,
                CollectatallModePayload.CODEC
        );
    }

    public static void sendAllPlayersMode(MinecraftServer server, CollectatallMode mode) {
        CollectatallModeState state = CollectatallModeState.getServerState(server);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new CollectatallModePayload(state.getMode().getId(), state.getTime(), state.getCount()));
        }
    }

    public static void sendToPlayerMode(MinecraftServer server, ServerPlayerEntity player) {
        CollectatallModeState state = CollectatallModeState.getServerState(server);
        ServerPlayNetworking.send(player, new CollectatallModePayload(
                state.getMode().getId(), state.getTime(), state.getCount()
        ));
    }
}
