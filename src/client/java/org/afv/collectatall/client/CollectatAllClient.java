package org.afv.collectatall.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.afv.collectatall.CollectatAll;
import org.afv.collectatall.client.mode.CollectatallModeStateClient;
import org.afv.collectatall.client.timer.CounterHud;
import org.afv.collectatall.client.timer.SpeedrunHud;
import org.afv.collectatall.client.timer.TimerHud;
import org.afv.collectatall.network.CollectatallModePayload;
import org.afv.collectatall.network.PlayerGetsItemPayload;
import org.afv.collectatall.network.PlayerRemainingCountPayload;
import org.afv.collectatall.network.ResponsePlayerPlayTimePayload;
import org.afv.collectatall.util.CollectatallMode;
import org.afv.collectatall.util.CollectatallModeState;

public class CollectatAllClient implements ClientModInitializer {
    private static long actPlayTime = 0;
    private static int blinkTicks = 0;
    private static int remainingCount = 0;
    private static Item prevItem;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(CollectatAll.ModID, "speedrun_timer"),
                SpeedrunHud::render
        );

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(CollectatAll.ModID, "counter_hud"),
                CounterHud::render
        );

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(CollectatAll.ModID, "timer_hud"),
                TimerHud::render
        );

        ClientPlayNetworking.registerGlobalReceiver(ResponsePlayerPlayTimePayload.ID,
                (payload, context) -> {
                    actPlayTime = payload.count();
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(PlayerGetsItemPayload.ID,
                (payload, context) -> {
                    blinkTicks = 100;
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(CollectatallModePayload.ID,
                (payload, context) -> {
                    CollectatallMode mode = CollectatallMode.getById(payload.mode());
                    if (mode != null) {
                        CollectatallModeStateClient.setMode(mode);
                        CollectatallModeStateClient.setMinutes(payload.minutes());
                        CollectatallModeStateClient.setCount(payload.count());
                    }
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(PlayerRemainingCountPayload.ID,
                (payload, context) -> {
                    remainingCount = payload.count();
                    String id = payload.item();
                    if (id != null && !id.equalsIgnoreCase("minecraft:air")) {
                        prevItem = Registries.ITEM.get(Identifier.of(payload.item()));
                    } else prevItem = null;
                }
        );
    }

    public static long getPlayTime() {
        return actPlayTime;
    }

    public static int getBlinkTicks() {
        return blinkTicks;
    }

    public static void decreaseBlinkTicks() {
        if (blinkTicks > 0) --blinkTicks;
    }

    public static int getRemainingCount() {
        return remainingCount;
    }

    public static Item getPrevItem() {
        return prevItem;
    }
}
