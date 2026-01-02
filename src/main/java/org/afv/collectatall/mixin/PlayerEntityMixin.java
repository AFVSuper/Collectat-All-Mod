package org.afv.collectatall.mixin;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.afv.collectatall.holder.PlayerItemsHolder;
import org.afv.collectatall.network.ResponsePlayerPlayTimePayload;
import org.afv.collectatall.util.CollectatallMode;
import org.afv.collectatall.util.CollectatallModeState;
import org.afv.collectatall.util.ItemListUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerItemsHolder {
    @Unique
    private final ArrayList<Item> remainingItems = ItemListUtil.getAllSurvivalItems();
    @Unique
    private Item prevItem;
    @Unique
    private long playTime = 0;
    @Unique
    private MutableText bookText;

    @Inject(method = "tick", at = @At("TAIL"))
    private void addTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        MinecraftServer server = player.getEntityWorld().getServer();
        if (playTime == -1) return;
        if (server != null) {
            CollectatallModeState state = CollectatallModeState.getServerState(server);
            if (!(CollectatallModeState.getServerState(server).getMode() == CollectatallMode.NORMAL)
                    && !player.isDead() && getRemainingCount() + state.getMaxItems() - ItemListUtil.ITEM_NUMBER > 0) {
                playTime++;
            }
        }
    }

    @Override
    public ArrayList<Item> getRemainingItems() {
        return remainingItems;
    }

    @Override
    public void obtainedItem(Item item) {
        this.remainingItems.remove(item);
    }

    @Override
    public int getRemainingCount() {
        return remainingItems.size();
    }

    @Override
    public boolean isItemNeeded(Item item) {
        return remainingItems.contains(item);
    }

    @Override
    public void setRemainingItems(List<Item> list) {
        remainingItems.clear();
        remainingItems.addAll(list);
    }

    @Override
    public void addItem(Item item) {
        if (!remainingItems.contains(item)) remainingItems.add(item);
    }

    @Override
    public long getPlayTicks() {
        return playTime;
    }

    @Override
    public void setPlayTicks(long ticks) {
        playTime = ticks;
    }

    @Override
    public Item getPrevItem() {
        return prevItem;
    }


    @Override
    public void setPrevItem(Item item) {
        prevItem = item;
    }

    @Override
    public void setBookText(MutableText text) {
        bookText = text;
    }

    @Override
    public MutableText getBookText() {
        return bookText;
    }

    /* -------------------- NBT SAVE -------------------- */

    @Inject(method = "writeCustomData", at = @At("HEAD"))
    private void writeLockedSlots(WriteView view, CallbackInfo ci) {
        ArrayList<String> ids = new ArrayList<>();
        for (Item item : remainingItems) {
            Identifier id = Registries.ITEM.getId(item);
            ids.add(id.toString());
        }
        view.put("remainingItemsIds", Codec.STRING.listOf(), ids);
        view.put("playTime", Codec.LONG, playTime);
        view.put("prevItem", Codec.STRING, Registries.ITEM.getId(prevItem).toString());
    }

    /* -------------------- NBT LOAD -------------------- */

    @Inject(method = "readCustomData", at = @At("HEAD"))
    private void readLockedSlots(ReadView view, CallbackInfo ci) {
        remainingItems.clear();

        view.getOptionalTypedListView("remainingItemsIds", Identifier.CODEC)
                .ifPresent(list -> {
                    for (Identifier id : list) {
                        Item item = Registries.ITEM.get(id);
                        if (item != Items.AIR) {
                            remainingItems.add(item);
                        }
                    }
                });

        playTime = view.getLong("playTime", 0);
        String id = view.getString("prevItem", null);
        if (id != null) {
            prevItem = Registries.ITEM.get(Identifier.of(id));
        }
    }
}
