package com.github.mrjimin.nexoscreens;

import com.github.mrjimin.nexoscreens.command.CommandHandler;
import com.github.mrjimin.nexoscreens.listener.EventListener;
import com.github.mrjimin.nexoscreens.manager.ScreenManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexoScreens extends JavaPlugin {
    public static NexoScreens INSTANCE;

    private ScreenManager screenManager;

    @Override
    public void onEnable() {
        INSTANCE = this;

        screenManager = new ScreenManager(this);
        CommandHandler commandHandler = new CommandHandler(screenManager);
        EventListener eventListener = new EventListener(screenManager);

        getServer().getPluginManager().registerEvents(eventListener, this);
        getServer().getScheduler().runTaskTimer(this, screenManager::tickScreens, 0L, 1L);
        getServer().getCommandMap().register("nexoscreens", commandHandler);

        screenManager.reload();
        getLogger().info("NexoScreens 2.0.1-26.2 enabled.");
    }

    @Override
    public void onDisable() {
        if (screenManager != null) {
            screenManager.clear();
        }
    }
}
