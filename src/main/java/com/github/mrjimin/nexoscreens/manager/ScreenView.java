package com.github.mrjimin.nexoscreens.manager;

import com.github.mrjimin.nexoscreens.util.CommandUtils;
import com.github.mrjimin.nexoscreens.util.ProtocolLibHook;
import org.bukkit.entity.Player;

import java.util.List;

public final class ScreenView {
    private final boolean originalInvulnerable;
    private final boolean movement;
    private final List<String> endCommands;
    private final long endTime;

    public ScreenView(boolean originalInvulnerable, boolean movement, List<String> endCommands, long endTime) {
        this.originalInvulnerable = originalInvulnerable;
        this.movement = movement;
        this.endCommands = List.copyOf(endCommands);
        this.endTime = endTime;
    }

    public boolean movement() {
        return movement;
    }

    public boolean isOver() {
        return System.currentTimeMillis() > endTime;
    }

    public void executeEndCommands(Player player) {
        CommandUtils.runCommands(endCommands);
        player.setInvulnerable(originalInvulnerable);
        ProtocolLibHook.showHUD(player);
    }
}
