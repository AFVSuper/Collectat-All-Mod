package org.afv.collectatall.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.text.object.AtlasTextObjectContents;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.afv.collectatall.CollectatAll;
import org.afv.collectatall.holder.PlayerItemsHolder;
import org.afv.collectatall.init.ModNetwork;
import org.afv.collectatall.util.CollectatallMode;
import org.afv.collectatall.util.CollectatallModeState;
import org.afv.collectatall.util.ItemListUtil;
import org.afv.collectatall.util.ItemToTextObject;

import java.util.ArrayList;
import java.util.List;

public class CollectatallCommand {
    private static final int MAX_ICONS_PER_ROW = 15;
    private static final int MAX_ROWS_PER_PAGE = 12;
    private static final int MAX_ICONS_PER_PAGE = MAX_ROWS_PER_PAGE * MAX_ICONS_PER_ROW;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("collectatall")
                .then(CommandManager.literal("check")
                        //.requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                        .executes(CollectatallCommand::executeCheck)
                        .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                                .executes(CollectatallCommand::executeCheckWithPage))
                )
                .then(CommandManager.literal("mode")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(CommandManager.argument("mode", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("normal");
                                    builder.suggest("speedrun");
                                    return builder.buildFuture();
                                })
                                .executes(CollectatallCommand::executeModeChange)
                        )
                        .then(CommandManager.literal("timed")
                                .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                                        .executes(CollectatallCommand::executeModeChangeTimed))
                        )
                        .then(CommandManager.literal("count")
                                .then(CommandManager.argument("number", IntegerArgumentType.integer(1))
                                        .executes(CollectatallCommand::executeModeChangeCount))
                        )
                )
                .then(CommandManager.literal("check-player")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(CollectatallCommand::executePlayerCheck)
                                .then(CommandManager.argument("page", IntegerArgumentType.integer())
                                        .executes(CollectatallCommand::executePlayerCheckWithPage)))
                )
                .then(CommandManager.literal("reset-time")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(CollectatallCommand::executeResetTime))
                )
        );
    }

    private static int executeResetTime(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        if (player instanceof PlayerItemsHolder holder) {
            holder.setPlayTicks(0);
            return 1;
        } else return 0;
    }

    private static int executeModeChange(CommandContext<ServerCommandSource> context) {
        String modeString = StringArgumentType.getString(context, "mode");
        CollectatallMode mode = CollectatallMode.getById(modeString);
        if (mode == null) return 0;

        changeMode(mode, 0, 0, context);
        return 1;
    }

    private static int executeModeChangeTimed(CommandContext<ServerCommandSource> context) {
        int minutes = IntegerArgumentType.getInteger(context, "minutes");

        changeMode(CollectatallMode.TIMED, minutes, 0, context);
        return 1;
    }

    private static int executeModeChangeCount(CommandContext<ServerCommandSource> context) {
        int count = IntegerArgumentType.getInteger(context, "number");

        changeMode(CollectatallMode.COUNT, 0, count, context);
        return 1;
    }

    private static void changeMode(CollectatallMode mode, int minutes, int count, CommandContext<ServerCommandSource> context) {
        CollectatallModeState state = CollectatallModeState.getServerState(context.getSource().getServer());
        state.setMode(mode);
        state.setTime(minutes);
        state.setCount(count);
        ModNetwork.sendAllPlayersMode(context.getSource().getServer(), mode);
    }

    private static int executePlayerCheck(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity source = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        showCheckPage(player, source, 1, "/collectatall check-player " + player.getName().getString());
        return 1;
    }

    private static int executePlayerCheckWithPage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int page = IntegerArgumentType.getInteger(context, "page");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        ServerPlayerEntity source = context.getSource().getPlayerOrThrow();
        showCheckPage(player, source, page, "/collectatall check-player " + player.getName().getString());
        return 1;
    }

    private static int executeCheck(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        showCheckPage(player, player, 1, "/collectatall check");
        return 1;
    }

    private static int executeCheckWithPage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int page = IntegerArgumentType.getInteger(context, "page");
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        showCheckPage(player, player, page, "/collectatall check");
        return 1;
    }

    private static void showCheckPage(ServerPlayerEntity player, ServerPlayerEntity source, int page, String command) throws CommandSyntaxException {
        if (!(player instanceof PlayerItemsHolder holder)) throw new SimpleCommandExceptionType(Text.translatable("command.collectatall.invalid_player")).create();
        ArrayList<Item> remainingItems = holder.getRemainingItems();
        int remainingNumber = remainingItems.size();
        int itemNumber = ItemListUtil.ITEM_NUMBER;
        int maxPage = remainingNumber / MAX_ICONS_PER_PAGE + 1;
        if (page > maxPage) throw new SimpleCommandExceptionType(Text.translatable("command.collectatall.invalid_page")).create();
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;
        CollectatallModeState state = CollectatallModeState.getServerState(server);

        source.sendMessage(Text.literal("\n\n\n"));
        source.sendMessage(Text.empty()
                .append(Text.literal("      -- ")
                        .formatted(Formatting.DARK_PURPLE)
                        .formatted(Formatting.BOLD))
                .append(Text.translatable("collectatall.check.title")
                        .formatted(Formatting.LIGHT_PURPLE)
                        .formatted(Formatting.BOLD)
                        .formatted(Formatting.UNDERLINE))
                .append(Text.literal(" --\n")
                        .formatted(Formatting.DARK_PURPLE)
                        .formatted(Formatting.BOLD))
        );
        source.sendMessage(Text.empty()
                .append(Text.literal("  > ")
                        .formatted(Formatting.BOLD)
                        .formatted(Formatting.GRAY))
                .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true)))
                .append(Text.literal(" "))
                .append(player.getName().copy())
        );
        source.sendMessage(Text.empty()
                .append(Text.literal("  > ")
                        .formatted(Formatting.BOLD)
                        .formatted(Formatting.DARK_GREEN))
                .append(Text.translatable("collectatall.check.obtained")
                        .append(Text.literal(": "))
                        .formatted(Formatting.RESET)
                        .formatted(Formatting.GREEN))
                .append(Text.literal((itemNumber - remainingNumber) + "/" + state.getMaxItems())
                        .formatted(Formatting.RESET)
                        .formatted(Formatting.YELLOW))
        );
        source.sendMessage(Text.empty()
                .append(Text.literal("  > ")
                        .formatted(Formatting.BOLD)
                        .formatted(Formatting.DARK_RED))
                .append(Text.translatable("collectatall.check.missing")
                        .append(Text.literal(": "))
                        .formatted(Formatting.RESET)
                        .formatted(Formatting.RED))
                .append(Text.literal((holder.getRemainingCount() + state.getMaxItems() - ItemListUtil.ITEM_NUMBER) + "\n")
                        .formatted(Formatting.RESET)
                        .formatted(Formatting.YELLOW))
        );
        int start = (page - 1) * MAX_ICONS_PER_PAGE;
        int count = 0;
        int line_counter = 0;
        int sent_counter = 0;
        int i = start;
        MutableText text = Text.empty().append(Text.literal("    | ").formatted(Formatting.GRAY));
        if (remainingNumber <= MAX_ROWS_PER_PAGE) {
            while (i < remainingNumber) {
                Item item = remainingItems.get(i);
                Rarity rarity = item.getComponents().get(DataComponentTypes.RARITY);
                text
                        .append(ItemToTextObject.getText(item).copy())
                        .append(Text.literal(" "))
                        .append(item.getName().copy().formatted(
                                rarity == null ? Formatting.WHITE : rarity.getFormatting()
                        ));
                source.sendMessage(text);
                text = Text.empty().append(Text.literal("    | ").formatted(Formatting.GRAY));
                i++;
            }
        } else {
            while (count < MAX_ICONS_PER_PAGE && i < remainingNumber) {
                Item item = remainingItems.get(i);
                Rarity rarity = item.getComponents().get(DataComponentTypes.RARITY);
                text.append((ItemToTextObject.getText(item).copy()).setStyle(
                        Style.EMPTY.withHoverEvent(
                                new HoverEvent.ShowText(item.getName().copy().formatted(
                                        rarity == null ? Formatting.WHITE : rarity.getFormatting()
                                ))
                        )
                ));
                if (count % MAX_ICONS_PER_ROW == 0) {
                    line_counter++;
                }
                if (count % MAX_ICONS_PER_ROW == MAX_ICONS_PER_ROW - 1) {
                    source.sendMessage(text);
                    sent_counter++;
                    text = Text.empty().append(Text.literal("    | ").formatted(Formatting.GRAY));
                } else text.append(" ");
                i++;
                count++;
            }
            if (page == maxPage && line_counter > sent_counter) {
                source.sendMessage(text);
                // CollectatAll.LOGGER.info("ExtraSend: {}, {}", line_counter, sent_counter);
            }
        }
        source.sendMessage(Text.empty());
        Text arrowL = page == 1 ?
                Text.literal("[ <-- ]  ").formatted(Formatting.DARK_GRAY) :
                Text.literal("[ <-- ]  ")
                        .setStyle(Style.EMPTY.withHoverEvent(
                                new HoverEvent.ShowText(Text.translatable("collectatall.check.go_to_page")
                                        .append(Text.literal(" " + (page - 1)))
                                        .formatted(Formatting.GRAY)))
                                .withClickEvent(
                                        new ClickEvent.RunCommand(command + " " + (page - 1))
                        ))
                        .formatted(Formatting.YELLOW);
        Text arrowR = page == maxPage ?
                Text.literal("  [ --> ]").formatted(Formatting.DARK_GRAY) :
                Text.literal("  [ --> ]")
                        .setStyle(Style.EMPTY.withHoverEvent(
                                new HoverEvent.ShowText(Text.translatable("collectatall.check.go_to_page")
                                        .append(Text.literal(" " + (page + 1)))
                                        .formatted(Formatting.GRAY)))
                                .withClickEvent(
                                        new ClickEvent.RunCommand(command + " " + (page + 1))
                        ))
                        .formatted(Formatting.YELLOW);
        source.sendMessage(Text.empty()
                .append(Text.literal("            "))
                .append(arrowL)
                .append(Text.translatable("collectatall.check.page").append(" " + page)
                        .formatted(Formatting.GRAY))
                .append(arrowR)
        );
        source.networkHandler.sendPacket(
                new PlaySoundS2CPacket(
                        SoundEvents.UI_BUTTON_CLICK,
                        SoundCategory.UI,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        1.0f, // volume
                        1.0f,  // pitch
                        0
                )
        );
    }
}
