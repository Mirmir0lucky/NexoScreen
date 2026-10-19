package com.github.mrjimin.nexoscreens.manager

import com.github.mrjimin.nexoscreens.config.ConfigHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class ScreenManager(private val plugin: JavaPlugin) {
    private val activeScreens: MutableMap<UUID, ScreenView> = mutableMapOf()
    private val screens: MutableMap<String, Screen> = mutableMapOf()
    private val configHandler = ConfigHandler(plugin)

    fun show(
        players: Set<Player>,
        screen: Screen,
        color: TextColor,
        text: Component,
        fadeInTicks: Long,
        stayTicks: Long,
        fadeOutTicks: Long
    ) {
        if (!Bukkit.isPrimaryThread()) {
            plugin.server.scheduler.runTask(plugin) { _ ->
                show(players, screen, color, text, fadeInTicks, stayTicks, fadeOutTicks)
            }
            return
        }

        val show = players.filterNot { activeScreens.containsKey(it.uniqueId) }.toSet()
        screen.show(show, color, text, fadeInTicks, stayTicks, fadeOutTicks) { uuid, view ->
            activeScreens[uuid] = view
        }
    }

    fun reload() {
        if (!Bukkit.isPrimaryThread()) {
            plugin.server.scheduler.runTask(plugin) { _ -> reload() }
            return
        }

        clear()
        configHandler.loadConfig()
        screens.putAll(configHandler.parseScreens())
    }

    fun clear() {
        activeScreens.forEach { (uuid, view) ->
            plugin.server.getPlayer(uuid)?.let { player ->
                player.clearTitle()
                view.executeEndCommands(player)
            }
        }
        activeScreens.clear()
        screens.clear()
    }

    fun tickScreens() {
        val iterator = activeScreens.iterator()
        while (iterator.hasNext()) {
            val (uuid, view) = iterator.next()
            val player = plugin.server.getPlayer(uuid)

            if (player == null) {
                iterator.remove()
                continue
            }

            if (!view.isOver()) continue

            view.executeEndCommands(player)
            iterator.remove()
        }
    }

    fun getScreen(id: String): Screen? = screens[id]
    fun getActiveScreen(uuid: UUID): ScreenView? = activeScreens[uuid]
    fun removeActiveScreen(uuid: UUID) = activeScreens.remove(uuid)
    fun getScreenKeys(): Set<String> = screens.keys
}