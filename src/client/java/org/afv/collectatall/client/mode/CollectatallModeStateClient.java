package org.afv.collectatall.client.mode;

import org.afv.collectatall.util.CollectatallMode;
import org.afv.collectatall.util.ItemListUtil;

public class CollectatallModeStateClient {
    private static CollectatallMode mode = CollectatallMode.NORMAL;
    private static int minutes = 0;
    private static int count = 0;

    public static CollectatallMode getMode() {
        return mode;
    }

    public static void setMode(CollectatallMode mode) {
        CollectatallModeStateClient.mode = mode;
    }

    public static int getMinutes() {
        return minutes;
    }

    public static void setMinutes(int minutes) {
        CollectatallModeStateClient.minutes = minutes;
    }

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        CollectatallModeStateClient.count = count;
    }

    public static int getMaxItems() {
        return count == 0 ? ItemListUtil.ITEM_NUMBER : count;
    }
}
