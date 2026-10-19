package com.github.mrjimin.nexoscreens.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TextUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Map<String, String> COLORS = new LinkedHashMap<>();

    static {
        COLORS.put("§a", "<green>");
        COLORS.put("§c", "<red>");
        COLORS.put("§b", "<aqua>");
        COLORS.put("§e", "<yellow>");
        COLORS.put("§6", "<gold>");
        COLORS.put("§d", "<light_purple>");
        COLORS.put("§f", "<white>");
        COLORS.put("§3", "<dark_aqua>");
        COLORS.put("§9", "<blue>");
        COLORS.put("§7", "<gray>");
        COLORS.put("§8", "<dark_gray>");
        COLORS.put("§4", "<dark_red>");
        COLORS.put("§1", "<dark_blue>");
        COLORS.put("§2", "<dark_green>");
        COLORS.put("§5", "<dark_purple>");
    }

    private TextUtil() {}

    public static Component toMMComponent(String input) {
        String result = input.replace('&', '§');
        for (Map.Entry<String, String> entry : COLORS.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return MINI_MESSAGE.deserialize(result);
    }
}
