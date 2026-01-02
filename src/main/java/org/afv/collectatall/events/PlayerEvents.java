package org.afv.collectatall.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.afv.collectatall.holder.PlayerItemsHolder;
import org.afv.collectatall.init.ModComponents;
import org.afv.collectatall.init.ModGamerules;
import org.afv.collectatall.init.ModNetwork;
import org.afv.collectatall.network.PlayerGetsItemPayload;
import org.afv.collectatall.network.PlayerRemainingCountPayload;
import org.afv.collectatall.network.ResponsePlayerPlayTimePayload;
import org.afv.collectatall.util.*;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerEvents {
    private static int tickCounter = 0;
    private static int packetTickCounter = 0;
    private static int delay = 0;
    private static final int INTERVAL_TICKS = 2;
    private static final ArrayList<ServerPlayerEntity> checked = new ArrayList<>();

    public static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(PlayerEvents::onServerTick);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (packetTickCounter >= 0) {
                if (!CollectatallModeState.getServerState(server).getMode().equals(CollectatallMode.NORMAL)) {
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        if (player instanceof PlayerItemsHolder holder) {
                            ServerPlayNetworking.send(player, new ResponsePlayerPlayTimePayload(holder.getPlayTicks()));
                        }
                    }
                } else {
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        if (!checked.contains(player)) {
                            ServerPlayNetworking.send(player, new ResponsePlayerPlayTimePayload(-1));
                            checked.add(player);
                        }
                    }
                }
                packetTickCounter = 0;
            } else packetTickCounter++;
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Reapply max health modifier after respawn
            if (newPlayer instanceof  PlayerItemsHolder holder && oldPlayer instanceof PlayerItemsHolder old) {
                holder.setRemainingItems(old.getRemainingItems());
                holder.setPlayTicks(old.getPlayTicks());
                holder.setPrevItem(old.getPrevItem());
            }
        });
        ServerPlayerEvents.JOIN.register(serverPlayerEntity -> {
            TeamUtils.createTeam(serverPlayerEntity);
            TeamUtils.updateSuffix(serverPlayerEntity);
            if (serverPlayerEntity instanceof PlayerItemsHolder holder) {
                Item item = holder.getPrevItem();
                ServerPlayNetworking.send(serverPlayerEntity, new PlayerRemainingCountPayload(holder.getRemainingCount(), item == null ? "minecraft:air" : item.toString()));
            }
            ModNetwork.sendToPlayerMode(serverPlayerEntity.getEntityWorld().getServer(), serverPlayerEntity);
        });
        ServerPlayerEvents.LEAVE.register(serverPlayerEntity -> {
            if (serverPlayerEntity instanceof PlayerItemsHolder holder) {
                ServerPlayNetworking.send(serverPlayerEntity, new PlayerRemainingCountPayload(holder.getRemainingCount(), null));
            }
        });
    }

    private static void onServerTick(MinecraftServer server) {
        if (tickCounter >= INTERVAL_TICKS && delay == 0) {
            // CollectatAll.LOGGER.info("EventCheck1");
            Scoreboard scoreboard = server.getScoreboard();
            ScoreboardObjective objective = scoreboard.getNullableObjective("collectatall_count");
            boolean isTimed = CollectatallModeState.getServerState(server).getMode() == CollectatallMode.TIMED;
            CollectatallModeState state = CollectatallModeState.getServerState(server);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerInventory inv = player.getInventory();
                if (player instanceof PlayerItemsHolder holder) {
                    int remaining = holder.getRemainingCount();
                    if (objective != null) scoreboard.getOrCreateScore(player, objective).setScore(ItemListUtil.ITEM_NUMBER - remaining);
                    if ((remaining + state.getMaxItems() - ItemListUtil.ITEM_NUMBER) <= 0) continue;
                    if (isTimed) {
                        if (holder.getPlayTicks() == -1) continue;
                        if (holder.getRemainingCount() > 0) {
                            if (state.getTime() * 1200L - holder.getPlayTicks() < 0) {
                                holder.setPlayTicks(-1);
                                Text title = Text.empty()
                                        .append(Text.literal("> ")
                                                .formatted(Formatting.DARK_RED)
                                                .formatted(Formatting.BOLD))
                                        .append(Text.translatable("collectatall.game.timed_timesup")
                                                .formatted(Formatting.RED)
                                                .formatted(Formatting.BOLD))
                                        .append(Text.literal(" <")
                                                .formatted(Formatting.DARK_RED)
                                                .formatted(Formatting.BOLD));
                                Text subtitle = Text.empty()
                                        .append(Text.translatable("collectatall.game.timed_sub_1")
                                                .formatted(Formatting.YELLOW))
                                        .append(Text.literal(" " + (ItemListUtil.ITEM_NUMBER - holder.getRemainingCount()) + " ")
                                                .formatted(Formatting.LIGHT_PURPLE)
                                                .formatted(Formatting.BOLD))
                                        .append(Text.translatable("collectatall.game.timed_sub_2")
                                                .formatted(Formatting.YELLOW));

                                broadcastChat(server, player, Text.empty()
                                        .append(TextUtils.getHeaderTimed())
                                        .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true)))
                                        .append(" ")
                                        .append(Text.translatable("collectatall.broadcast.initial_sign")
                                                .formatted(Formatting.YELLOW))
                                        .append(player.getName().copy()
                                                .withColor(0xffd92e))
                                        .append(Text.translatable("collectatall.broadcast.apostrophe_space"))
                                        .append(Text.translatable("collectatall.broadcast.timed_timesup_1")
                                                .formatted(Formatting.YELLOW))
                                        .append(Text.literal(" " + (ItemListUtil.ITEM_NUMBER - holder.getRemainingCount()) + " ")
                                                .formatted(Formatting.BOLD)
                                                .formatted(Formatting.LIGHT_PURPLE))
                                        .append(Text.translatable("collectatall.broadcast.timed_timesup_2")
                                                .formatted(Formatting.YELLOW))
                                );

                                sendTitle(player, title, subtitle, 10, 60, 20);
                                sendSound(player, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.UI, player.getX(), player.getY(), player.getZ(), 1f, 0.8f);
                            }
                        }
                    }
                    for (int i = 0; i < 41; i++) {
                        ItemStack stack = inv.getStack(i);
                        Item item = stack.getItem();
                        if (holder.isItemNeeded(item)) {
                            if (stack.contains(ModComponents.ITEM_CHECKED)) continue;
                            execObtainItem(player, inv.getStack(i), item, server);
                            delay = 5;
                            break;
                        }
                    }
                }
                if (delay > 0) break;
            }
            tickCounter = 0;
        }
        if (delay > 0) delay--;
        tickCounter++;
    }

    private static void execObtainItem(ServerPlayerEntity player, ItemStack stack, Item item, MinecraftServer server) {
        if (player instanceof PlayerItemsHolder holder) {
            ServerPlayNetworking.send(player, new PlayerGetsItemPayload());
            holder.obtainedItem(item);
            ObtainMode obtainMode = player.getEntityWorld().getGameRules().getValue(ModGamerules.MODIFY_OBTAINED_GAMERULE);
            switch (obtainMode) {
                case ObtainMode.MARK -> markStack(stack, player);
                case ObtainMode.REMOVE -> stack.setCount(stack.getCount() - 1);
                case ObtainMode.MIXED -> {
                    if (stack.getMaxCount() == 1) markStack(stack, player);
                    else {
                        Rarity rarity = item.getComponents().get(DataComponentTypes.RARITY);
                        if (rarity != Rarity.COMMON) markStack(stack, player);
                        else {
                            stack.setCount(stack.getCount() - 1);
                        }
                    }
                }
                default -> {}
            }
            holder.setPrevItem(item);
            TeamUtils.updateSuffix(player);
            CollectatallModeState state = CollectatallModeState.getServerState(server);
            if (player.getEntityWorld().getGameRules().getValue(ModGamerules.RARE_BROADCAST_GAMERULE)) {
                Rarity rarity = item.getComponents().get(DataComponentTypes.RARITY);
                if (rarity != null && rarity != Rarity.COMMON) {
                    broadcastChat(server, player, Text.empty()
                            .append(TextUtils.getRareHeader())
                            .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true)))
                            .append(" ")
                            .append(player.getName().copy()
                                    .withColor(0xffffff))
                            .append(" ")
                            .append(Text.translatable("collectatall.broadcast.rare")
                                    .formatted(Formatting.WHITE))
                            .append(Text.literal(": ")
                                    .formatted(Formatting.WHITE))
                            .append(ItemToTextObject.getText(item))
                            .append(" ")
                            .append(item.getName().copy()
                                    .formatted(rarity.getFormatting())
                            )
                    );
                }
            }
            player.sendMessage(Text.empty()
                            .append(ItemToTextObject.getText(item))
                            .append(Text.literal(" "))
                            .append(item.getName().copy().formatted(Formatting.GREEN))
                            .append(Text.literal(" ✔").withColor(0x00FF00))
                    , true);
            if ((holder.getRemainingCount() + state.getMaxItems() - ItemListUtil.ITEM_NUMBER) == 0) {
                Text congrats = Text.empty()
                        .append(Text.literal("> ")
                                .formatted(Formatting.DARK_GREEN)
                                .formatted(Formatting.BOLD))
                        .append(Text.translatable("collectatall.game.congrats")
                                .formatted(Formatting.GREEN)
                                .formatted(Formatting.BOLD))
                        .append(Text.literal(" <")
                                .formatted(Formatting.DARK_GREEN)
                                .formatted(Formatting.BOLD));

                Text subtitle = Text.empty();
                Text chatMsg = Text.empty();
                CollectatallMode mode = CollectatallModeState.getServerState(server).getMode();

                if (mode.equals(CollectatallMode.SPEEDRUN)) {
                    subtitle = Text.empty()
                            .append(Text.translatable("collectatall.game.congrats_sub_speedrun").append(Text.literal(": "))
                                    .formatted(Formatting.YELLOW))
                            .append(Text.literal(TextUtils.formatTime(holder.getPlayTicks()))
                                    .formatted(Formatting.LIGHT_PURPLE)
                                    .formatted(Formatting.BOLD));
                    chatMsg = Text.empty()
                            .append(TextUtils.getHeaderVictory())
                            .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true)))
                            .append(" ")
                            .append(player.getName().copy()
                                    .withColor(0x00ff00))
                            .append(" ")
                            .append(Text.translatable("collectatall.broadcast.speedrun_1")
                                    .formatted(Formatting.GREEN))
                            .append(Text.literal(" " + ItemListUtil.ITEM_NUMBER + " ")
                                    .formatted(Formatting.BOLD)
                                    .formatted(Formatting.LIGHT_PURPLE))
                            .append(Text.translatable("collectatall.broadcast.speedrun_2")
                                    .formatted(Formatting.GREEN))
                            .append(Text.literal(" " + TextUtils.formatTime(holder.getPlayTicks()))
                                    .formatted(Formatting.BOLD)
                                    .formatted(Formatting.LIGHT_PURPLE));
                } else if (mode.equals(CollectatallMode.NORMAL)) {
                    subtitle = Text.empty()
                            .append(Text.translatable("collectatall.game.congrats_sub_normal_1").append(Text.literal(": "))
                                    .formatted(Formatting.YELLOW))
                            .append(Text.literal(ItemListUtil.ITEM_NUMBER + " ")
                                    .formatted(Formatting.LIGHT_PURPLE)
                                    .formatted(Formatting.BOLD))
                            .append(Text.translatable("collectatall.game.congrats_sub_normal_2")
                                    .formatted(Formatting.YELLOW));
                    chatMsg = Text.empty()
                            .append(TextUtils.getHeaderVictory())
                            .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true)))
                            .append(" ")
                            .append(Text.translatable("collectatall.broadcast.initial_sign")
                                    .formatted(Formatting.GREEN))
                            .append(player.getName().copy()
                                    .withColor(0x00ff00))
                            .append(" ")
                            .append(Text.translatable("collectatall.broadcast.normal_1")
                                    .formatted(Formatting.GREEN))
                            .append(Text.literal(" " + ItemListUtil.ITEM_NUMBER + " ")
                                    .formatted(Formatting.BOLD)
                                    .formatted(Formatting.LIGHT_PURPLE))
                            .append(Text.translatable("collectatall.broadcast.normal_2")
                                    .formatted(Formatting.GREEN));
                } else if (mode.equals(CollectatallMode.TIMED)) {
                    subtitle = Text.empty()
                            .append(Text.translatable("collectatall.game.congrats_sub_timed")
                                    .formatted(Formatting.YELLOW));
                    chatMsg = Text.empty()
                            .append(TextUtils.getHeaderVictory())
                            .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true)))
                            .append(" ")
                            .append(player.getName().copy()
                                    .withColor(0x00ff00))
                            .append(" ")
                            .append(Text.translatable("collectatall.broadcast.timed_1")
                                    .formatted(Formatting.GREEN))
                            .append(Text.literal(" " + ItemListUtil.ITEM_NUMBER + " ")
                                    .formatted(Formatting.BOLD)
                                    .formatted(Formatting.LIGHT_PURPLE))
                            .append(Text.translatable("collectatall.broadcast.timed_2")
                                    .formatted(Formatting.GREEN))
                            .append(Text.literal(" " + TextUtils.formatTime(holder.getPlayTicks()))
                                    .formatted(Formatting.BOLD)
                                    .formatted(Formatting.LIGHT_PURPLE));
                } else if (mode.equals(CollectatallMode.COUNT)) {
                    subtitle = Text.empty()
                            .append(Text.translatable("collectatall.game.congrats_sub_count_1")
                                    .formatted(Formatting.YELLOW))
                            .append(Text.literal(" " + state.getMaxItems() + " ")
                                    .formatted(Formatting.LIGHT_PURPLE)
                                    .formatted(Formatting.BOLD))
                            .append(Text.translatable("collectatall.game.congrats_sub_count_2")
                                    .formatted(Formatting.YELLOW))
                            .append(Text.literal(" " + TextUtils.formatTime(holder.getPlayTicks()))
                                    .formatted(Formatting.BOLD)
                                    .formatted(Formatting.LIGHT_PURPLE));
                    chatMsg = Text.empty()
                            .append(TextUtils.getHeaderVictory())
                            .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true)))
                            .append(" ")
                            .append(player.getName().copy()
                                    .withColor(0x00ff00))
                            .append(" ")
                            .append(Text.translatable("collectatall.broadcast.count_1")
                                    .formatted(Formatting.GREEN))
                            .append(Text.literal(" " + state.getMaxItems() + " ")
                                    .formatted(Formatting.BOLD)
                                    .formatted(Formatting.LIGHT_PURPLE))
                            .append(Text.translatable("collectatall.broadcast.count_2")
                                    .formatted(Formatting.GREEN))
                            .append(Text.literal(" " + TextUtils.formatTime(holder.getPlayTicks()))
                                    .formatted(Formatting.BOLD)
                                    .formatted(Formatting.LIGHT_PURPLE));
                }

                broadcastChat(server, player, chatMsg);
                sendSound(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 1f);
                sendTitle(player, congrats, subtitle, 10, 120, 40);
            }
            ServerPlayNetworking.send(player, new PlayerRemainingCountPayload(holder.getRemainingCount(), item.toString()));
            player.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(
                            Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP),
                            SoundCategory.UI,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            1.0f, // volume
                            1.5f,  // pitch
                            0
                    )
            );
            player.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(
                            SoundEvents.BLOCK_NOTE_BLOCK_CHIME,
                            SoundCategory.UI,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            1.0f, // volume
                            1f,  // pitch
                            0
                    )
            );
        }
    }

    public static void sendSound(ServerPlayerEntity player, RegistryEntry<SoundEvent> sound, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        player.networkHandler.sendPacket(
                new PlaySoundS2CPacket(
                        sound,
                        category,
                        x, y, z,
                        volume, pitch,
                        0
                )
        );
    }

    public static void sendSound(ServerPlayerEntity player, SoundEvent sound, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        sendSound(player, Registries.SOUND_EVENT.getEntry(sound), category, x, y, z, volume, pitch);
    }

    public static void sendTitle(ServerPlayerEntity player, Text main, Text sub, int fadeIn, int stay, int fadeOut) {
        // durations
        player.networkHandler.sendPacket(
                new TitleFadeS2CPacket(fadeIn, stay, fadeOut)
        );

        // subtitle
        if (sub != null) {
            player.networkHandler.sendPacket(
                    new SubtitleS2CPacket(sub)
            );
        }

        // main title
        if (main != null) {
            player.networkHandler.sendPacket(
                    new TitleS2CPacket(main)
            );
        }
    }

    public static void broadcastChat(MinecraftServer server, ServerPlayerEntity source, Text msg) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(msg);
            if (player != source) sendSound(player, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 1f);
        }
    }

    public static void markStack(ItemStack stack, ServerPlayerEntity player) {
        stack.set(ModComponents.ITEM_CHECKED, true);
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
        Text mark = Text.empty()
                .append(Text.translatable("collectatall.mark").append(Text.literal(": "))
                        .formatted(Formatting.RESET)
                        .withColor(0xffd073))
                .append(Text.object(new PlayerTextObjectContents(ProfileComponent.ofDynamic(player.getUuid()), true))
                        .formatted(Formatting.RESET)
                        .formatted(Formatting.ITALIC)
                        .formatted(Formatting.WHITE))
                .append(" ")
                .append(player.getName().copy()
                        .formatted(Formatting.RESET)
                        .formatted(Formatting.WHITE));
        stack.set(
                DataComponentTypes.LORE,
                Objects.requireNonNullElse(lore, LoreComponent.DEFAULT).with(mark)
        );
    }
}
