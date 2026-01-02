package org.afv.collectatall.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.object.AtlasTextObjectContents;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.afv.collectatall.holder.PlayerItemsHolder;

public class TeamUtils {
    public static void createTeam(PlayerEntity player) {
        Scoreboard scoreboard = player.getEntityWorld().getScoreboard();
        String teamName = player.getName().getString();

        // Team already exists
        if (scoreboard.getTeam(teamName) != null) {
            return;
        }

        Team team = scoreboard.addTeam(teamName);
//        team.setPrefix(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true))
//                .append(Text.literal(" "))
//                .formatted(Formatting.WHITE));

        scoreboard.addScoreHolderToTeam(player.getName().getString(), team);
    }

    public static void updateSuffix(PlayerEntity player) {
        Team team = player.getScoreboardTeam();
        if (team == null) return;
        if (!(player instanceof PlayerItemsHolder holder)) return;
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;
        CollectatallModeState state = CollectatallModeState.getServerState(server);
        Item prev = holder.getPrevItem();
        Text prevText = Text.empty();
        if (prev != null && !prev.equals(Items.AIR)) {
            Rarity rarity = prev.getComponents().get(DataComponentTypes.RARITY);
            prevText = Text.empty()
                    .append(Text.literal("\n> ")
                            .formatted(Formatting.GRAY)
                            .formatted(Formatting.BOLD))
                    .append(Text.translatable("collectatall.game.hud.prev").append(": ")
                            .formatted(Formatting.WHITE))
                    .append(ItemToTextObject.getText(prev))
                    .append(" ")
                    .append(prev.getName().copy()
                            .formatted(rarity == null ? Formatting.WHITE : rarity.getFormatting()));
        }
        holder.setBookText(Text.empty()
                .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true)))
                .append(" ")
                .append(player.getName().copy())
                .append("\n")
                .append(Text.literal("> ")
                        .formatted(Formatting.DARK_GREEN)
                        .formatted(Formatting.BOLD))
                .append(Text.translatable("collectatall.check.obtained").append(": ")
                        .formatted(Formatting.GREEN))
                .append(Text.literal((ItemListUtil.ITEM_NUMBER - holder.getRemainingCount()) + "/" + state.getMaxItems())
                        .formatted(Formatting.YELLOW))
                .append("\n")
                .append(Text.literal("> ")
                        .formatted(Formatting.DARK_RED)
                        .formatted(Formatting.BOLD))
                .append(Text.translatable("collectatall.check.missing").append(": ")
                        .formatted(Formatting.RED))
                .append(Text.literal("" + (holder.getRemainingCount() + state.getMaxItems() - ItemListUtil.ITEM_NUMBER))
                        .formatted(Formatting.YELLOW))
                .append(prevText)
                .append("\n")
                .append(Text.empty()
                        .append("(")
                        .append(Text.translatable("collectatall.check.click_for_more"))
                        .append(")")
                        .formatted(Formatting.DARK_GRAY)
                        .formatted(Formatting.ITALIC))
        );
        team.setSuffix(Text.empty()
                .append(" ")
                .append(Text.object(new AtlasTextObjectContents(
                        Identifier.ofVanilla("items"),
                        Identifier.ofVanilla("item/knowledge_book")
                )))
                .formatted(Formatting.WHITE).setStyle(Style.EMPTY
                        .withHoverEvent(
                                new HoverEvent.ShowText(holder.getBookText())
                        )
                        .withClickEvent(
                                new ClickEvent.RunCommand("/collectatall check-player " + player.getName().getString())
                        )
                )
        );
    }
}
