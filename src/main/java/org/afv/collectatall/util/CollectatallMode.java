package org.afv.collectatall.util;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum CollectatallMode {
    NORMAL("collectatall.mode.normal", "normal"),
    SPEEDRUN("collectatall.mode.speedrun", "speedrun"),
    TIMED("collectatall.mode.timed", "timed"),
    COUNT("collectatall.mode.count", "count");
    // TODO: RANDOM("collectatall.mode.random", "random");
    // TODO: BINGO("collectatall.mode.bingo", "bingo");

    private final String translationKey;
    private final String id;

    CollectatallMode(String translationKey, String id) {
        this.translationKey = translationKey;
        this.id = id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Text getDescription() {
        return Text.empty()
                .append(Text.translatable(translationKey).append(": ")
                        .formatted(Formatting.WHITE))
                .append(Text.translatable(translationKey + ".desc")
                        .formatted(Formatting.GRAY));
    }

    public String getId() {
        return id;
    }

    public static CollectatallMode getById(String id) {
        for (CollectatallMode mode : CollectatallMode.values()) {
            if (mode.getId().equalsIgnoreCase(id)) return mode;
        }
        return null;
    }

    public static final Codec<CollectatallMode> CODEC =
            Codec.STRING.xmap(CollectatallMode::valueOf, Enum::name);
}
