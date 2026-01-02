package org.afv.collectatall.client.timer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.afv.collectatall.CollectatAll;
import org.afv.collectatall.client.CollectatAllClient;
import org.afv.collectatall.client.mode.CollectatallModeStateClient;
import org.afv.collectatall.util.CollectatallMode;
import org.afv.collectatall.util.ColorUtil;
import org.afv.collectatall.util.ItemListUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static org.afv.collectatall.util.TextUtils.formatTime;

public class TimerHud {
    public static boolean toast = false;

    private static float portion = 0f;
    private static boolean soundDelay = false;
    private static long toastTime = -1;
    private static long showTime = 0;
    private static boolean visible = true;
    private static long startTime = -1;
    private static float toastOffset = 0f; // current animated offset
    private static final int TOAST_WIDTH = 160; // 160; // vanilla toast width in pixels

    public static void render(@NotNull DrawContext context, @NotNull RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) return;
        if (CollectatallModeStateClient.getMode() != CollectatallMode.TIMED) return;
        if (client.options.hudHidden) return;

        long ticks = CollectatAllClient.getPlayTime();
        int timerMinute = CollectatallModeStateClient.getMinutes();

        int remaining = CollectatAllClient.getRemainingCount();
        boolean finish = (ItemListUtil.ITEM_NUMBER - remaining) >= CollectatallModeStateClient.getMaxItems();
        long timerTicks = Math.max(0, timerMinute * 1200L - ticks);
        if (ticks == -1) timerTicks = 0;
        // CollectatAll.LOGGER.info("Timer: {}, {}", timerMinute, timerTicks);
        String time = formatTime(timerTicks + 18);

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

        int color = ColorUtil.lerpColor(0xFFFF5555, 0xFFFFFF55, (float)timerTicks/(timerMinute * 1200L));
        if (finish) color = 0xFF00FF00;
        else if (timerTicks <= 200) {
            if (timerTicks - 2 <= 0) {
                color = 0xFF777777;
            } else if ((timerTicks - 2) % 20 > 10) {
                color = 0xFFFF2222;
                if (!soundDelay) {
                    client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.2f);
                    soundDelay = true;
                }
            } else soundDelay = false;
        }
        context.drawTextWithShadow(
                client.textRenderer,
                text,
                x,
                y,
                color
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