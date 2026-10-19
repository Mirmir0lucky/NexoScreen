package com.github.mrjimin.nexoscreens.util;

import com.github.mrjimin.nexoscreens.NexoScreens;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

public final class NexoHook {
    private NexoHook() {}

    public static Component glyphComponent(String glyphId, TextColor color) {
        try {
            Object nexo = resolveNexoInstance();
            if (nexo == null) return Component.empty();

            Object fontManager = invokeNoArg(nexo, "fontManager", "getFontManager");
            if (fontManager == null) return Component.empty();

            Object glyph = invokeOneArg(fontManager, glyphId,
                    "glyphFromID", "glyphFromId", "getGlyphFromID", "getGlyphFromId");
            glyph = unwrapOptional(glyph);
            if (glyph == null) return Component.empty();

            Object component = invokeNoArg(glyph, "glyphComponent", "getGlyphComponent", "component", "getComponent");
            if (component instanceof Component adventureComponent) {
                return adventureComponent.color(color);
            }
        } catch (Throwable throwable) {
            NexoScreens.INSTANCE.getLogger().warning("Nexo glyph could not be resolved: " + glyphId + " (" + throwable.getClass().getSimpleName() + ")");
        }
        return Component.empty();
    }

    public static void reloadAll(CommandSender sender) {
        try {
            Class<?> reload = Class.forName("com.nexomc.nexo.commands.ReloadCommand");
            for (Method method : reload.getMethods()) {
                if (!method.getName().equals("reloadAll") || !Modifier.isStatic(method.getModifiers())) continue;
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && params[0].isAssignableFrom(sender.getClass())) {
                    method.invoke(null, sender);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        // Fallback for Nexo builds where ReloadCommand moved.
        if (!Bukkit.dispatchCommand(sender, "nexo reload all")) {
            Bukkit.dispatchCommand(sender, "nexo reload");
        }
    }

    private static Object resolveNexoInstance() {
        try {
            Class<?> clazz = Class.forName("com.nexomc.nexo.NexoPlugin");
            for (String name : new String[]{"instance", "getInstance"}) {
                try {
                    Method method = clazz.getMethod(name);
                    if (Modifier.isStatic(method.getModifiers())) {
                        Object result = method.invoke(null);
                        if (result != null) return result;
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
        } catch (ClassNotFoundException ignored) {
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Nexo");
        return plugin;
    }

    private static Object unwrapOptional(Object object) {
        if (object instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return object;
    }

    private static Object invokeNoArg(Object target, String... names) throws Exception {
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name);
                return method.invoke(target);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static Object invokeOneArg(Object target, Object arg, String... names) throws Exception {
        for (String name : names) {
            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != 1) continue;
                Class<?> type = method.getParameterTypes()[0];
                if (arg == null || type.isAssignableFrom(arg.getClass())) {
                    return method.invoke(target, arg);
                }
            }
        }
        return null;
    }
}
