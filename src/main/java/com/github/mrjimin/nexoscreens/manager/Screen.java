package com.github.mrjimin.nexoscreens.manager;

import com.github.mrjimin.nexoscreens.util.CommandUtils;
import com.github.mrjimin.nexoscreens.util.NexoHook;
import com.github.mrjimin.nexoscreens.util.PapiHook;
import com.github.mrjimin.nexoscreens.util.ProtocolLibHook;
import com.github.mrjimin.nexoscreens.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class Screen {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final String id;
    private final boolean invulnerable;
    private final boolean movement;
    private final List<String> startCommands;
    private final List<String> endCommands;

    public Screen(String id, boolean invulnerable, boolean movement, List<String> startCommands, List<String> endCommands) {
        this.id = id;
        this.invulnerable = invulnerable;
        this.movement = movement;
        this.startCommands = List.copyOf(startCommands);
        this.endCommands = List.copyOf(endCommands);
    }

    public void show(Set<Player> players,
                     TextColor color,
                     Component text,
                     long fadeInTicks,
                     long stayTicks,
                     long fadeOutTicks,
                     BiConsumer<UUID, ScreenView> consumer) {
        long fadeInMs = fadeInTicks * 50L;
        long stayMs = stayTicks * 50L;
        long fadeOutMs = fadeOutTicks * 50L;
        long totalMs = fadeInMs + stayMs + fadeOutMs;
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeInMs),
                Duration.ofMillis(stayMs),
                Duration.ofMillis(fadeOutMs)
        );

        for (Player player : players) {
            Component finalText = text;
            if (PapiHook.isAvailable()) {
                String serialized = MINI_MESSAGE.serialize(text);
                finalText = TextUtil.toMMComponent(PapiHook.apply(player, serialized));
            }

            List<String> finalStart = startCommands;
            List<String> finalEnd = endCommands;
            if (PapiHook.isAvailable()) {
                finalStart = startCommands.stream().map(command -> PapiHook.apply(player, command)).collect(Collectors.toList());
                finalEnd = endCommands.stream().map(command -> PapiHook.apply(player, command)).collect(Collectors.toList());
            }

            CommandUtils.runCommands(finalStart);

            boolean originalInvulnerable = player.isInvulnerable();
            if (invulnerable) player.setInvulnerable(true);
            ProtocolLibHook.hideHUD(player);

            consumer.accept(player.getUniqueId(), new ScreenView(
                    originalInvulnerable,
                    movement,
                    finalEnd,
                    System.currentTimeMillis() + totalMs
            ));

            Component glyph = NexoHook.glyphComponent("nexoscreens_" + id, color);
            player.showTitle(Title.title(finalText, glyph, times));
        }
    }

    public static Screen parse(String id, ConfigurationSection section, ConfigurationSection defaults) {
        boolean invulnerable = section.getBoolean("invulnerable", defaults.getBoolean("invulnerable"));
        boolean movement = section.getBoolean("movement", defaults.getBoolean("movement"));
        List<String> start = section.getStringList("commands.start");
        if (start.isEmpty()) start = defaults.getStringList("commands.start");
        List<String> end = section.getStringList("commands.end");
        if (end.isEmpty()) end = defaults.getStringList("commands.end");
        return new Screen(id, invulnerable, movement, start, end);
    }
}
