package org.afv.collectatall.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import org.afv.collectatall.CollectatAll;
import org.afv.collectatall.commands.CollectatallCommand;
import org.afv.collectatall.commands.DebugCommand;

public class ModCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        if (CollectatAll.isDebugActive()) DebugCommand.register(dispatcher, commandRegistryAccess);
        CollectatallCommand.register(dispatcher);
    }
}
