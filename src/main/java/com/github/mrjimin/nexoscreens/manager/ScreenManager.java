package com.github.mrjimin.nexoscreens.manager;

import com.github.mrjimin.nexoscreens.config.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ScreenManager {
    private final JavaPlugin plugin;
    private final Map<UUID, ScreenView> activeScreens = new HashMap<>();
    private final Map<String, Screen> screens = new HashMap<>();
    private final ConfigHandler configHandler;

    public ScreenManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configHandler = new ConfigHandler(plugin);
    }

    public void show(Set<Player> players,
                     Screen screen,
                     TextColor color,
                     Component text,
                     long fadeInTicks,
                     long stayTicks,
                     long fadeOutTicks) {
        if (!Bukkit.isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> show(players, screen, color, text, fadeInTicks, stayTicks, fadeOutTicks));
            return;
        }

        Set<Player> visible = new HashSet<>();
        for (Player player : players) {
            if (!activeScreens.containsKey(player.getUniqueId())) visible.add(player);
        }

        screen.show(visible, color, text, fadeInTicks, stayTicks, fadeOutTicks, activeScreens::put);
    }

    public void reload() {
        if (!Bukkit.isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin, this::reload);
            return;
        }

        clear();
        configHandler.loadConfig();
        screens.putAll(configHandler.parseScreens());
    }

    public void clear() {
        for (Map.Entry<UUID, ScreenView> entry : activeScreens.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.clearTitle();
                entry.getValue().executeEndCommands(player);
            }
        }
        activeScreens.clear();
        screens.clear();
    }

    public void tickScreens() {
        Iterator<Map.Entry<UUID, ScreenView>> iterator = activeScreens.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ScreenView> entry = iterator.next();
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            if (!entry.getValue().isOver()) continue;
            entry.getValue().executeEndCommands(player);
            iterator.remove();
        }
    }

    public Screen getScreen(String id) {
        return screens.get(id);
    }

    public ScreenView getActiveScreen(UUID uuid) {
        return activeScreens.get(uuid);
    }

    public void removeActiveScreen(UUID uuid) {
        activeScreens.remove(uuid);
    }

    public Set<String> getScreenKeys() {
        return Set.copyOf(screens.keySet());
    }
}
