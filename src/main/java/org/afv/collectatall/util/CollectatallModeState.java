package org.afv.collectatall.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import org.afv.collectatall.CollectatAll;
import org.afv.collectatall.init.ModNetwork;

public class CollectatallModeState extends PersistentState {

    private CollectatallMode mode;
    private int time;
    private int count;

    public static final Codec<CollectatallModeState> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    CollectatallMode.CODEC
                            .fieldOf("mode")
                            .forGetter(CollectatallModeState::getMode),

                    Codec.INT
                            .optionalFieldOf("time", 0)
                            .forGetter(state ->
                                    state.mode == CollectatallMode.TIMED ? state.time : 0
                            ),
                    Codec.INT
                            .optionalFieldOf("count", 0)
                            .forGetter(state ->
                                    state.mode == CollectatallMode.COUNT ? state.count : 0
                            )
            ).apply(instance, CollectatallModeState::new));

    public static final PersistentStateType<CollectatallModeState> TYPE = new PersistentStateType<>(
            CollectatAll.ModID + "_mode",
            CollectatallModeState::new,
            CODEC,
            null
    );

    public CollectatallModeState() {
        this(CollectatallMode.NORMAL, 0, 0);
    }

    public int getTime() {
        return time;
    }

    public CollectatallModeState(CollectatallMode mode, int time, int count) {
        this.mode = mode;
        this.time = (mode == CollectatallMode.TIMED) ? time : 0;
        this.count = (mode == CollectatallMode.COUNT) ? count : 0;
    }

    public CollectatallMode getMode() {
        return mode;
    }

    public int getCount() {
        return count;
    }

    public void setMode(CollectatallMode mode) {
        this.mode = mode;
        this.markDirty();
    }

    public void setTime(int time) {
        if (this.mode == CollectatallMode.TIMED) {
            this.time = time;
            this.markDirty();
        }
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getMaxItems() {
        return count == 0 ? ItemListUtil.ITEM_NUMBER : count;
    }

    public static CollectatallModeState getServerState(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        assert world != null;
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }
}