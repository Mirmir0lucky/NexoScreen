package com.github.mrjimin.nexoscreens.listener;

import com.github.mrjimin.nexoscreens.manager.ScreenManager;
import com.github.mrjimin.nexoscreens.manager.ScreenView;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class EventListener implements Listener {
    private final ScreenManager screenManager;

    public EventListener(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        ScreenView view = screenManager.getActiveScreen(uuid);
        if (view == null) return;
        view.executeEndCommands(event.getPlayer());
        screenManager.removeActiveScreen(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        ScreenView view = screenManager.getActiveScreen(event.getPlayer().getUniqueId());
        if (view == null || view.isOver() || view.movement()) return;
        event.setCancelled(true);
    }
}
