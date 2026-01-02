package org.afv.collectatall.client.timer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.afv.collectatall.CollectatAll;
import org.afv.collectatall.client.CollectatAllClient;
import org.afv.collectatall.client.mode.CollectatallModeStateClient;
import org.afv.collectatall.util.CollectatallMode;
import org.afv.collectatall.util.CollectatallModeState;
import org.afv.collectatall.util.ItemListUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static org.afv.collectatall.util.TextUtils.formatTime;

public class SpeedrunHud {
    public static boolean toast = false;

    private static float portion = 0f;
    private static long toastTime = -1;
    private static long showTime = 0;
    private static boolean visible = true;
    private static long startTime = -1;
    private static float toastOffset = 0f; // current animated offset
    private static final int TOAST_WIDTH = 160; // 160; // vanilla toast width in pixels

    public static void render(@NotNull DrawContext context, @NotNull RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) return;
        CollectatallMode mode = CollectatallModeStateClient.getMode();
        if (mode != CollectatallMode.SPEEDRUN && mode != CollectatallMode.COUNT) return;
        if (client.options.hudHidden) return;

        long ticks = CollectatAllClient.getPlayTime();

        if (ticks == -1) return;

        int remaining = CollectatAllClient.getRemainingCount();
        boolean finish = (ItemListUtil.ITEM_NUMBER - remaining) >= CollectatallModeStateClient.getMaxItems();
        String time = formatTime(ticks);

        int positiveEffects = 0;

        if (!(client.currentScreen instanceof InventoryScreen || client.currentScreen instanceof CreativeInventoryScreen)) {
            for (StatusEffectInstance effect : client.player.getStatusEffects()) {
                if (effect.shouldShowIcon()) {
                    Optional<RegistryKey<StatusEffect>> key = effect.getEffectType().getKey();
                    if (key.isPresent()) {
                        StatusEffect effect1 = Registries.STATUS_EFFECT.get(key.get());
                        if (effect1 != null && effect1.isBeneficial()) positiveEffects++;
                    }
                }
            }
        }

        Text text = Text.literal(time).formatted(finish ? Formatting.BOLD : Formatting.RESET);

        int screenWidth = client.getWindow().getScaledWidth();
        int textWidth = client.textRenderer.getWidth(text);

        double optDisplayTime = client.options.getNotificationDisplayTime().getValue();

        if (toast) {
            long l = Util.getMeasuringTimeMs();
            if (startTime == -1L) {
                startTime = l;
                visible = true;
            }
            if (visible && l - startTime < 600) {
                toastTime = l;
            }

            showTime = l - toastTime;
            updateToastOffset(l);
            boolean new_vis = showTime < 5000.0f * optDisplayTime;
            if (new_vis != visible) {
                startTime = l - (long)((int)((1.0F - portion) * 600.0F));
                visible = new_vis;
            }

            if (!visible && l - startTime > 600L) {
                toast = false;
                startTime = -1L;
                showTime = -1L;
                toastTime = -1L;
            }
        }

        int x = (int) Math.min(screenWidth - textWidth - 9 - (24 * positiveEffects), screenWidth - textWidth - toastOffset - 9);
        int y = 9;

        // CollectatAll.LOGGER.info("ToastOffset: {}", toastOffset);

        context.drawTextWithShadow(
                client.textRenderer,
                text,
                x,
                y,
                finish ? 0xFF00FF00 : 0xFFFFFF55
        );
    }

    public static void updateToastOffset(long l) {
        float f = MathHelper.clamp((float)(l - startTime) / 600.0F, 0.0F, 1.0F);
        f *= f;
        if (visible) {
            portion = f;
        } else {
            portion = 1.0F - f;
        }

        toastOffset = TOAST_WIDTH * portion;
    }
}