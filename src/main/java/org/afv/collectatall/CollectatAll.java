package org.afv.collectatall;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.FixedNumberFormat;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.afv.collectatall.events.PlayerEvents;
import org.afv.collectatall.init.ModCommands;
import org.afv.collectatall.init.ModNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectatAll implements ModInitializer {
    public static final String ModID = "collectatall";
    public static final Logger LOGGER = LoggerFactory.getLogger(ModID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModCommands.register(dispatcher, registryAccess);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(CollectatAll::onServerStarting);

        ModNetwork.registerPayloads();

        PlayerEvents.registerEvents();
    }

    private static void onServerStarting(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        String objectiveName = "collectatall_count";

        // Prevent duplicate creation (important on reloads / singleplayer)
        if (scoreboard.getNullableObjective(objectiveName) != null) {
            return;
        }

        ScoreboardObjective objective = scoreboard.addObjective(
                objectiveName,
                ScoreboardCriterion.DUMMY,
                Text.translatable("scoreboard.collectatall.count"),
                ScoreboardCriterion.RenderType.INTEGER,
                true,
                new StyledNumberFormat(Style.EMPTY.withBold(true).withColor(TextColor.fromFormatting(Formatting.AQUA)))
        );

        // Optional: set display slot
        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.LIST, objective);
    }
}
