package org.afv.collectatall.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.afv.collectatall.holder.PlayerItemsHolder;
import org.afv.collectatall.util.ItemListUtil;
import org.afv.collectatall.util.ItemToTextObject;

import java.util.ArrayList;
import java.util.List;

public class DebugCommand {
    private static int ind = 0;
    private static final ArrayList<Item> list = ItemListUtil.getAllSurvivalItems();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(CommandManager.literal("mod-debug")
                .then(CommandManager.literal("next")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                        .executes(DebugCommand::executeNext)
                )
                .then(CommandManager.literal("reset")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .executes(context -> ind = 0)
                )
                .then(CommandManager.literal("test")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .executes(context -> {
                            ItemToTextObject.test2(context.getSource().getPlayerOrThrow());
                            return 1;
                        })
                )
                .then(CommandManager.literal("set_first")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(CommandManager.argument("count", IntegerArgumentType.integer(0))
                            .executes(context -> {
                                int count = IntegerArgumentType.getInteger(context, "count");
                                if (context.getSource().getEntity() instanceof ServerPlayerEntity player) {
                                    if (player instanceof PlayerItemsHolder holder) {
                                        ArrayList<Item> orig = ItemListUtil.getAllSurvivalItems();
                                        ArrayList<Item> list = new ArrayList<>();
                                        for (int i = 0; i < count; i++) {
                                            list.add(orig.get(i));
                                        }
                                        holder.setRemainingItems(list);
                                    }
                                }
                                return 1;
                        })))
                .then(CommandManager.literal("add_item")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof ServerPlayerEntity player) {
                                if (player instanceof PlayerItemsHolder holder) {
                                    Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
                                    holder.addItem(item);
                                }
                            }
                            return 1;
                        })))
        );
    }

    private static int executeNext(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        MutableText msg = Text.empty();
        for (int i = 0; i < 9; i++) {
            Item item = list.get(ind);
            player.getInventory().setStack(i, new ItemStack(item));
            ind++;
            msg.append(ItemToTextObject.getText(item));
            if (i < 8) msg.append(Text.literal(" "));
            // player.sendMessage(Text.literal("Given item " + ind + "/" + len + " of the list."));
        }
        player.sendMessage(Text.literal("Given items ").append(msg));
        return 1;
    }
}
