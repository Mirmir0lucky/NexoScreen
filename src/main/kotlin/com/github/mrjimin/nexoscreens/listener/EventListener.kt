package com.github.mrjimin.nexoscreens.listener

import com.github.mrjimin.nexoscreens.manager.ScreenManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class EventListener(private val screenManager: ScreenManager) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val view = screenManager.getActiveScreen(uuid) ?: return

        view.executeEndCommands(player)
        screenManager.removeActiveScreen(uuid)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onMove(event: PlayerMoveEvent) {
        val view = screenManager.getActiveScreen(event.player.uniqueId) ?: return
        if (view.isOver() || view.movement) return

        event.isCancelled = true
    }
}