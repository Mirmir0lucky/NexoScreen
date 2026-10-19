package com.github.mrjimin.nexoscreens.util;

import com.github.mrjimin.nexoscreens.NexoScreens;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class ProtocolLibHook {
    private static final int CHANGE_GAME_MODE = 3;
    private static final Map<UUID, Scoreboard> SCOREBOARDS = new HashMap<>();

    private ProtocolLibHook() {}

    public static void hideHUD(Player player) {
        SCOREBOARDS.putIfAbsent(player.getUniqueId(), player.getScoreboard());
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        if (player.getGameMode() == GameMode.SPECTATOR) return;
        setClientGamemode(player, GameMode.SPECTATOR);
        refreshAbilities(player);
    }

    public static void showHUD(Player player) {
        Scoreboard scoreboard = SCOREBOARDS.remove(player.getUniqueId());
        if (scoreboard != null) player.setScoreboard(scoreboard);

        if (player.getGameMode() == GameMode.SPECTATOR) return;
        setClientGamemode(player, player.getGameMode());
        Bukkit.getScheduler().runTaskLater(NexoScreens.INSTANCE, () -> refreshAbilities(player), 5L);
    }

    private static void refreshAbilities(Player player) {
        if (player.isOnline()) {
            player.setAllowFlight(player.getAllowFlight());
        }
    }

    private static void setClientGamemode(Player player, GameMode gameMode) {
        try {
            Class<?> serverPacketTypes = Class.forName("com.comphenix.protocol.PacketType$Play$Server");
            Field field = serverPacketTypes.getField("GAME_STATE_CHANGE");
            Object packetType = field.get(null);

            Class<?> packetContainerClass = Class.forName("com.comphenix.protocol.events.PacketContainer");
            Object packet = createPacket(packetContainerClass, packetType);

            Object gameStateModifier = packetContainerClass.getMethod("getGameStateIDs").invoke(packet);
            writeModifier(gameStateModifier, 0, CHANGE_GAME_MODE);

            Object floatModifier = packetContainerClass.getMethod("getFloat").invoke(packet);
            writeModifier(floatModifier, 0, gameModeToId(gameMode));

            Class<?> protocolLibraryClass = Class.forName("com.comphenix.protocol.ProtocolLibrary");
            Object manager = protocolLibraryClass.getMethod("getProtocolManager").invoke(null);
            sendPacket(manager, player, packet);
        } catch (Throwable throwable) {
            NexoScreens.INSTANCE.getLogger().log(Level.WARNING,
                    "ProtocolLib could not send the HUD packet to " + player.getName(), throwable);
        }
    }

    private static Object createPacket(Class<?> packetContainerClass, Object packetType) throws Exception {
        for (Constructor<?> constructor : packetContainerClass.getConstructors()) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length == 1 && params[0].isAssignableFrom(packetType.getClass())) {
                return constructor.newInstance(packetType);
            }
        }
        throw new NoSuchMethodException("PacketContainer(PacketType)");
    }

    private static void writeModifier(Object modifier, int index, Object value) throws Exception {
        for (Method method : modifier.getClass().getMethods()) {
            if (!method.getName().equals("write") || method.getParameterCount() != 2) continue;
            Class<?>[] params = method.getParameterTypes();
            if ((params[0] == int.class || params[0] == Integer.class)) {
                method.invoke(modifier, index, value);
                return;
            }
        }
        throw new NoSuchMethodException("StructureModifier.write(int, value)");
    }

    private static void sendPacket(Object manager, Player player, Object packet) throws Exception {
        for (Method method : manager.getClass().getMethods()) {
            if (!method.getName().equals("sendServerPacket") || method.getParameterCount() != 2) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params[0].isAssignableFrom(player.getClass()) && params[1].isAssignableFrom(packet.getClass())) {
                method.invoke(manager, player, packet);
                return;
            }
        }
        throw new NoSuchMethodException("ProtocolManager.sendServerPacket(Player, PacketContainer)");
    }

    private static float gameModeToId(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> 0f;
            case CREATIVE -> 1f;
            case ADVENTURE -> 2f;
            case SPECTATOR -> 3f;
        };
    }
}
