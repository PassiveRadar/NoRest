package com.radar.norest.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

// I don't imagine ppl needing
public class CommandManager {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerZombieStatusCommand(dispatcher);
    }

    private static void registerZombieStatusCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("debugNoRest")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    String status = ZombieManager.getStatusReport(context.getSource().getLevel());
                    context.getSource().sendSuccess(() -> 
                            Component.literal(status), true);
                    return 1;
                }));
    }

} 