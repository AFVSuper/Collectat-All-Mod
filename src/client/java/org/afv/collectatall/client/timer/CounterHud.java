package org.afv.collectatall.client.timer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.afv.collectatall.CollectatAll;
import org.afv.collectatall.client.CollectatAllClient;
import org.afv.collectatall.client.mode.CollectatallModeStateClient;
import org.afv.collectatall.holder.PlayerItemsHolder;
import org.afv.collectatall.util.ItemListUtil;
import org.afv.collectatall.util.ItemToTextObject;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static org.afv.collectatall.util.TextUtils.formatTime;

public class CounterHud {
    public static void render(@NotNull DrawContext context, @NotNull RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) return;
        if (client.options.hudHidden) return;

        boolean blink = CollectatAllClient.getBlinkTicks() > 0;

        Text text = Text.empty()
                .append(Text.translatable("collectatall.game.hud.count").append(Text.literal(": "))
                        .formatted(blink ? Formatting.DARK_GREEN : Formatting.DARK_AQUA))
                .append(Text.empty()
                        .append(Text.literal("" + (ItemListUtil.ITEM_NUMBER - CollectatAllClient.getRemainingCount())))
                        .append(Text.literal("/"))
                        .append(Text.literal("" + CollectatallModeStateClient.getMaxItems()))
                                .formatted(blink ? Formatting.GREEN : Formatting.AQUA));
        Text text2 = Text.empty();
        Item item = CollectatAllClient.getPrevItem();
        if (item != null) {
            Rarity rarity = item.getComponents().get(DataComponentTypes.RARITY);
            text2 = Text.empty()
                    .append(Text.translatable("collectatall.game.hud.prev").append(Text.literal(": "))
                            .formatted(Formatting.GRAY))
                    .append(Text.empty()
                            .append(ItemToTextObject.getText(item))
                            .append(" ")
                            .append(item.getName().copy()
                                    .formatted(rarity == null ? Formatting.WHITE : rarity.getFormatting())));
        }

        int x = 9;
        int y = 9;

        context.drawTextWithShadow(
                client.textRenderer,
                text,
                x,
                y,
                0xFFFFFFFF
        );
        context.drawTextWithShadow(
                client.textRenderer,
                text2,
                x,
                y + 10,
                0xFFFFFFFF
        );

        CollectatAllClient.decreaseBlinkTicks();
    }
}
