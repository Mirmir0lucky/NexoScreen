package com.github.mrjimin.nexoscreens.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class PapiHook {
    private static volatile Method setPlaceholders;
    private static volatile boolean lookedUp;

    private PapiHook() {}

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static String apply(Player player, String text) {
        if (!isAvailable()) return text;

        try {
            Method method = resolve();
            if (method == null) return text;
            Object result = method.invoke(null, player, text);
            return result instanceof String string ? string : text;
        } catch (Throwable throwable) {
            return text;
        }
    }

    private static Method resolve() throws ClassNotFoundException {
        if (lookedUp) return setPlaceholders;
        synchronized (PapiHook.class) {
            if (lookedUp) return setPlaceholders;
            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            for (Method method : papi.getMethods()) {
                if (!method.getName().equals("setPlaceholders") || !Modifier.isStatic(method.getModifiers())) continue;
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 2 && params[1] == String.class) {
                    setPlaceholders = method;
                    break;
                }
            }
            lookedUp = true;
            return setPlaceholders;
        }
    }
}
