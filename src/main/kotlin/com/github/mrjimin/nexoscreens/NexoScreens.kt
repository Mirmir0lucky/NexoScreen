package com.github.mrjimin.nexoscreens

import com.github.mrjimin.nexoscreens.command.CommandHandler
import com.github.mrjimin.nexoscreens.listener.EventListener
import com.github.mrjimin.nexoscreens.manager.ScreenManager
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import java.util.Locale

class NexoScreens : JavaPlugin() {

    private lateinit var screenManager: ScreenManager
    private lateinit var commandHandler: CommandHandler
    private lateinit var eventListener: EventListener

    companion object {
        lateinit var INSTANCE: NexoScreens
            private set
    }

    override fun onEnable() {
        INSTANCE = this
        Metrics(this, 25025)

        screenManager = ScreenManager(this)
        commandHandler = CommandHandler(screenManager)
        eventListener = EventListener(screenManager)

        server.pluginManager.registerEvents(eventListener, this)
        server.scheduler.runTaskTimer(this, screenManager::tickScreens, 0L, 1L)

        server.commandMap.register(name.lowercase(Locale.ROOT), commandHandler)

        screenManager.reload()
        logger.info("NexoScreens ${pluginMeta.version} enabled for Paper/Leaf 26.2.")
    }

    override fun onDisable() {
        if (::screenManager.isInitialized) {
            screenManager.clear()
        }
    }
}