package com.github.mrjimin.nexoscreens.util

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.github.mrjimin.nexoscreens.NexoScreens
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import java.util.UUID
import java.util.logging.Level

object ProtocolLib {
    // ClientboundGameEventPacket.CHANGE_GAME_MODE
    private const val CHANGE_GAME_MODE = 3

    private val scoreboards: MutableMap<UUID, Scoreboard> = HashMap()

    private fun setClientGamemode(player: Player, gameMode: GameMode) {
        val packet = PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE)

        // Do not inspect Mojang/NMS fields by index. ProtocolLib exposes this converter
        // specifically so packet layout changes (including 26.x) do not break us.
        packet.gameStateIDs.write(0, CHANGE_GAME_MODE)
        packet.float.write(0, gamemodeToId(gameMode))

        sendPacket(player, packet)
    }

    private fun refreshAbilities(player: Player) {
        if (player.isOnline) {
            player.allowFlight = player.allowFlight
        }
    }

    fun hideHUD(player: Player) {
        scoreboards.putIfAbsent(player.uniqueId, player.scoreboard)
        player.scoreboard = Bukkit.getScoreboardManager().newScoreboard

        if (player.gameMode == GameMode.SPECTATOR) return

        setClientGamemode(player, GameMode.SPECTATOR)
        refreshAbilities(player)
    }

    fun showHUD(player: Player) {
        scoreboards.remove(player.uniqueId)?.let { player.scoreboard = it }

        if (player.gameMode == GameMode.SPECTATOR) return

        setClientGamemode(player, player.gameMode)
        Bukkit.getScheduler().runTaskLater(
            NexoScreens.INSTANCE,
            Runnable { refreshAbilities(player) },
            5L
        )
    }

    private fun gamemodeToId(gameMode: GameMode): Float = when (gameMode) {
        GameMode.SURVIVAL -> 0f
        GameMode.CREATIVE -> 1f
        GameMode.ADVENTURE -> 2f
        GameMode.SPECTATOR -> 3f
    }

    private fun sendPacket(player: Player, packet: PacketContainer) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
        } catch (throwable: Throwable) {
            NexoScreens.INSTANCE.logger.log(
                Level.WARNING,
                "ProtocolLib could not send the HUD packet to ${player.name}.",
                throwable
            )
        }
    }
}