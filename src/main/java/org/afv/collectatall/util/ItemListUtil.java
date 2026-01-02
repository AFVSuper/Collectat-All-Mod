package org.afv.collectatall.util;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemListUtil {
    private static final Set<Item> IMPOSSIBLE_ITEMS = Set.of(
            Items.COMMAND_BLOCK,
            Items.COMMAND_BLOCK_MINECART,
            Items.CHAIN_COMMAND_BLOCK,
            Items.REPEATING_COMMAND_BLOCK,
            Items.KNOWLEDGE_BOOK,
            Items.LIGHT,
            Items.STRUCTURE_BLOCK,
            Items.STRUCTURE_VOID,
            Items.BARRIER,
            Items.JIGSAW,
            Items.SPAWNER,
            Items.VAULT,
            Items.TRIAL_SPAWNER,
            Items.TEST_BLOCK,
            Items.TEST_INSTANCE_BLOCK,
            Items.DEBUG_STICK,
            Items.BEDROCK,
            Items.REINFORCED_DEEPSLATE,
            Items.END_PORTAL_FRAME,
            Items.INFESTED_COBBLESTONE,
            Items.INFESTED_CHISELED_STONE_BRICKS,
            Items.INFESTED_DEEPSLATE,
            Items.INFESTED_STONE,
            Items.INFESTED_STONE_BRICKS,
            Items.INFESTED_CRACKED_STONE_BRICKS,
            Items.INFESTED_MOSSY_STONE_BRICKS,
            Items.FROGSPAWN,
            Items.FARMLAND,
            Items.DIRT_PATH,
            Items.BUDDING_AMETHYST,
            Items.PLAYER_HEAD,
            Items.PETRIFIED_OAK_SLAB,
            Items.AIR,
            Items.CHORUS_PLANT
    );

    private static final ArrayList<Item> STATIC_LIST = calculateAllSurvivalItems();
    public static final int ITEM_NUMBER = STATIC_LIST.size();

    public static ArrayList<Item> getAllSurvivalItems() {
        return new ArrayList<>(STATIC_LIST);
    }

    private static ArrayList<Item> calculateAllSurvivalItems() {
        ArrayList<Item> items = new ArrayList<>();

        for (Item item : Registries.ITEM) {
            if (Registries.ITEM.getId(item).getPath().contains("spawn_egg")) continue;
            if (IMPOSSIBLE_ITEMS.contains(item)) continue;
            items.add(item);
        }

        return items;
    }
}
