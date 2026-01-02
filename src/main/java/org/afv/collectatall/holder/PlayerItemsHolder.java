package org.afv.collectatall.holder;

import net.minecraft.item.Item;
import net.minecraft.text.MutableText;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public interface PlayerItemsHolder {
    ArrayList<Item> getRemainingItems();
    void obtainedItem(Item item);
    int getRemainingCount();
    boolean isItemNeeded(Item item);
    void setRemainingItems(List<Item> list);
    void addItem(Item item);
    long getPlayTicks();
    void setPlayTicks(long ticks);
    Item getPrevItem();
    void setPrevItem(Item item);
    void setBookText(MutableText text);
    MutableText getBookText();
}
