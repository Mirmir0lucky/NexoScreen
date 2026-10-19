package com.github.mrjimin.nexoscreens.command;

import com.github.mrjimin.nexoscreens.manager.Screen;
import com.github.mrjimin.nexoscreens.manager.ScreenManager;
import com.github.mrjimin.nexoscreens.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CommandHandler extends Command {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final ScreenManager screenManager;

    public CommandHandler(ScreenManager screenManager) {
        super("nexoscreens");
        this.screenManager = screenManager;
        setAliases(List.of("ns"));
        setDescription("NexoScreens main command");
        setPermission("nexoscreens.command");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender instanceof Player player && !player.hasPermission("nexoscreens.command")) return false;

        if (args.length == 0) {
            sender.sendMessage(TextUtil.toMMComponent(
                    "<gradient:#9055FF:#13E2DA>NexoScreens <gray>| <#b8bbc2>Available commands\n" +
                    "<#6f737d>/nexoscreens <#b8bbc2>reload <#6f737d>» <#b8bbc2>plugin reload\n" +
                    "<#6f737d>/nexoscreens <#b8bbc2>show <#43c9fa>args <#6f737d>» <#b8bbc2>show screens"));
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            screenManager.reload();
            sender.sendMessage(TextUtil.toMMComponent("<gradient:#9055FF:#13E2DA>NexoScreens <gray>| <gray>Screens reloaded!"));
            return true;
        }

        if (args[0].equalsIgnoreCase("show")) {
            if (args.length < 7) return usage(sender);
            Set<Player> players = parsePlayers(args[1]);
            if (players == null) return usage(sender);
            Screen screen = screenManager.getScreen(args[2]);
            if (screen == null) return usage(sender);

            Long fadeIn = parseLong(args[3]);
            Long stay = parseLong(args[4]);
            Long fadeOut = parseLong(args[5]);
            TextColor color = TextColor.fromHexString(args[6]);
            if (fadeIn == null || stay == null || fadeOut == null || color == null) return usage(sender);

            Component text = args.length < 8
                    ? Component.empty()
                    : MINI_MESSAGE.deserialize(String.join(" ", Arrays.copyOfRange(args, 7, args.length)));

            screenManager.show(players, screen, color, text, fadeIn, stay, fadeOut);
            sender.sendMessage(TextUtil.toMMComponent("<gradient:#9055FF:#13E2DA>NexoScreens <gray>| <gray>Showing screen!"));
            return true;
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1 -> list.addAll(List.of("show", "reload"));
            case 2 -> {
                if (args[0].equalsIgnoreCase("show")) {
                    for (Player player : Bukkit.getOnlinePlayers()) list.add(player.getName());
                    list.add("*");
                }
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("show")) list.addAll(screenManager.getScreenKeys());
            }
            case 4, 5, 6 -> {
                if (args[0].equalsIgnoreCase("show")) list.addAll(List.of("5", "10", "20", "40", "60", "80", "100"));
            }
            case 7 -> {
                if (args[0].equalsIgnoreCase("show")) list.addAll(List.of("#0000ff", "#000000", "#FFFFFF"));
            }
            case 8 -> {
                if (args[0].equalsIgnoreCase("show")) list.add("<green>Test");
            }
            default -> { }
        }

        if (args.length > 0) {
            String prefix = args[args.length - 1];
            list.removeIf(value -> !value.startsWith(prefix));
        }
        return list;
    }

    private Set<Player> parsePlayers(String raw) {
        Set<Player> players = new HashSet<>();
        if (raw.equals("*")) {
            players.addAll(Bukkit.getOnlinePlayers());
            return players;
        }
        Player player = Bukkit.getPlayer(raw);
        if (player == null) return null;
        players.add(player);
        return players;
    }

    private boolean usage(CommandSender sender) {
        sender.sendMessage(TextUtil.toMMComponent(
                "<#6f737d>/ns <#b8bbc2>show <#43c9fa>player <#fa8943>screen <#43fa8c>fadeIn stay fadeOut <#faf443>color <#fa4362>text"));
        return false;
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
