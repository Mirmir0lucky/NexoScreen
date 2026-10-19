package com.github.mrjimin.nexoscreens.util;

import com.github.mrjimin.nexoscreens.NexoScreens;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class CommandUtils {
    private CommandUtils() {}

    public static void runCommands(List<String> commands) {
        CommandSender sender = Bukkit.getConsoleSender();
        long commandDelayTicks = 0L;

        for (String command : commands) {
            if (command.startsWith("delay ")) {
                try {
                    commandDelayTicks += Long.parseLong(command.substring("delay ".length()).trim()) * 20L;
                } catch (NumberFormatException ignored) {
                    // Keep original behavior: invalid delay adds zero ticks.
                }
                continue;
            }

            long delay = commandDelayTicks;
            Bukkit.getScheduler().runTaskLater(
                    NexoScreens.INSTANCE,
                    () -> Bukkit.dispatchCommand(sender, command),
                    delay
            );
        }
    }
}
