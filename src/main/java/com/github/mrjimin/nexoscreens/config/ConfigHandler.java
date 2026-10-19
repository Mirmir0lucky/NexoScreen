package com.github.mrjimin.nexoscreens.config;

import com.github.mrjimin.nexoscreens.manager.Screen;
import com.github.mrjimin.nexoscreens.util.NexoHook;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public final class ConfigHandler {
    private final JavaPlugin plugin;

    public ConfigHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        saveResources();
    }

    public Map<String, Screen> parseScreens() {
        Map<String, Screen> screens = new HashMap<>();
        ConfigurationSection defaults = plugin.getConfig().getConfigurationSection("defaults");
        ConfigurationSection screensSection = plugin.getConfig().getConfigurationSection("screens");
        if (defaults == null || screensSection == null) return screens;

        for (String key : screensSection.getKeys(false)) {
            ConfigurationSection section = screensSection.getConfigurationSection(key);
            if (section != null) screens.put(key, Screen.parse(key, section, defaults));
        }
        return screens;
    }

    private void saveResources() {
        File pluginsFolder = plugin.getDataFolder().getParentFile();
        if (pluginsFolder == null) return;

        File nexoDir = new File(pluginsFolder, "Nexo");
        File glyphFile = new File(nexoDir, "glyphs/nexoscreens.yml");
        File packFile = new File(nexoDir, "pack/external_packs/nexoscreens.zip");

        boolean changed = false;
        changed |= copyIfMissing("default/nexoscreens.yml", glyphFile);
        changed |= copyIfMissing("default/assets.zip", packFile);

        if (changed && plugin.getServer().getPluginManager().isPluginEnabled("Nexo")) {
            NexoHook.reloadAll(plugin.getServer().getConsoleSender());
        }
    }

    private boolean copyIfMissing(String resource, File target) {
        if (target.exists()) return false;
        File parent = target.getParentFile();
        if (parent != null) parent.mkdirs();

        try (InputStream input = plugin.getResource(resource)) {
            if (input == null) {
                plugin.getLogger().warning("Bundled resource missing: " + resource);
                return false;
            }
            Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not write " + target + ": " + exception.getMessage());
            return false;
        }
    }
}
