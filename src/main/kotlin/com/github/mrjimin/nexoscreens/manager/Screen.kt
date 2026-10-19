package com.github.mrjimin.nexoscreens.manager

import com.github.mrjimin.nexoscreens.util.CommandUtils
import com.github.mrjimin.nexoscreens.util.ProtocolLib
import com.github.mrjimin.nexoscreens.util.toMMComponent
import com.nexomc.nexo.NexoPlugin
import com.nexomc.nexo.glyphs.Glyph
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID

data class Screen(
    val id: String,
    val invulnerable: Boolean,
    val movement: Boolean,
    val startCommands: List<String>,
    val endCommands: List<String>
) {
    private var glyph: Glyph? = null
    private val miniMessage = MiniMessage.miniMessage()

    fun show(
        players: Set<Player>,
        color: TextColor,
        text: Component,
        fadeInTicks: Long,
        stayTicks: Long,
        fadeOutTicks: Long,
        consumer: (UUID, ScreenView) -> Unit
    ) {
        val fadeInMs = fadeInTicks * 50L
        val stayMs = stayTicks * 50L
        val fadeOutMs = fadeOutTicks * 50L
        val totalMs = fadeInMs + stayMs + fadeOutMs
        val titleTimes = Title.Times.times(
            Duration.ofMillis(fadeInMs),
            Duration.ofMillis(stayMs),
            Duration.ofMillis(fadeOutMs)
        )
        val hasPapi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")

        players.forEach { player ->
            val finalText = if (hasPapi) {
                PlaceholderAPI.setPlaceholders(player, miniMessage.serialize(text)).toMMComponent()
            } else {
                text
            }
            val title = createTitle(color, finalText, titleTimes)

            val finalStartCommands = if (hasPapi) {
                startCommands.map { PlaceholderAPI.setPlaceholders(player, it) }
            } else {
                startCommands
            }
            val finalEndCommands = if (hasPapi) {
                endCommands.map { PlaceholderAPI.setPlaceholders(player, it) }
            } else {
                endCommands
            }

            CommandUtils.runCommands(finalStartCommands)

            val originalInvulnerable = player.isInvulnerable
            if (invulnerable) {
                player.isInvulnerable = true
            }
            ProtocolLib.hideHUD(player)

            consumer(
                player.uniqueId,
                ScreenView(
                    originalInvulnerable,
                    movement,
                    finalEndCommands,
                    System.currentTimeMillis() + totalMs
                )
            )
            player.showTitle(title)
        }
    }

    private fun createTitle(color: TextColor, text: Component, times: Title.Times): Title {
        glyph = glyph ?: NexoPlugin.instance().fontManager().glyphFromID("nexoscreens_$id")
            ?: throw IllegalStateException("Glyph not found: nexoscreens_$id")

        return Title.title(text, glyph!!.glyphComponent().color(color), times)
    }

    companion object {
        fun parse(id: String, section: ConfigurationSection, defaults: ConfigurationSection): Screen {
            return Screen(
                id,
                section.getBoolean("invulnerable", defaults.getBoolean("invulnerable")),
                section.getBoolean("movement", defaults.getBoolean("movement")),
                section.getStringList("commands.start").ifEmpty { defaults.getStringList("commands.start") },
                section.getStringList("commands.end").ifEmpty { defaults.getStringList("commands.end") }
            )
        }
    }
}

data class ScreenView(
    val originalInvulnerable: Boolean,
    val movement: Boolean,
    val endCommands: List<String>,
    val endTime: Long
) {
    fun isOver(): Boolean = System.currentTimeMillis() > endTime

    fun executeEndCommands(player: Player) {
        CommandUtils.runCommands(endCommands)
        player.isInvulnerable = originalInvulnerable
        ProtocolLib.showHUD(player)
    }
}