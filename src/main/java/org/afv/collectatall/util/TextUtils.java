package org.afv.collectatall.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtils {
    public static String formatTime(long ticks) {
        long totalSeconds = ticks / 20;

        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private static final Text RARE_HEADER = Text.empty()
            .append(Text.literal("[")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.DARK_AQUA))
            .append(Text.literal("☄")
                    .formatted(Formatting.AQUA))
            .append(Text.literal("]")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" > ")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.WHITE));

    private static final Text TIMED_HEADER = Text.empty()
            .append(Text.literal("[")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.DARK_RED))
            .append(Text.literal("⌚")
                    .formatted(Formatting.RED))
            .append(Text.literal("]")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.DARK_RED))
            .append(Text.literal(" > ")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.WHITE));

    private static final Text VICTORY_HEADER = Text.empty()
            .append(Text.literal("[")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.DARK_GREEN))
            .append(Text.literal("✔")
                    .formatted(Formatting.GREEN)
                    .formatted(Formatting.BOLD))
            .append(Text.literal("]")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" > ")
                    .formatted(Formatting.BOLD)
                    .formatted(Formatting.WHITE));

    public static Text getHeaderTimed() { return TIMED_HEADER.copy(); }
    public static Text getHeaderVictory() { return VICTORY_HEADER.copy(); }
    public static Text getRareHeader() { return RARE_HEADER.copy(); }
}
