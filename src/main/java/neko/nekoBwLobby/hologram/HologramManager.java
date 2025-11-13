package neko.nekoBwLobby.hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HologramManager {
    private static boolean holographicDisplaysEnabled = false;
    private static final String PLUGIN_NAME = "HolographicDisplays";

    static {
        try {
            if (Bukkit.getPluginManager().getPlugin(PLUGIN_NAME) != null) {
                holographicDisplaysEnabled = true;
            }
        } catch (Exception e) {
            holographicDisplaysEnabled = false;
        }
    }

    public static boolean isHolographicDisplaysEnabled() {
        return holographicDisplaysEnabled;
    }

    public static Object createHologram(Plugin plugin, Location location) {
        if (!holographicDisplaysEnabled) {
            return null;
        }

        try {
            // 尝试使用新版API
            Class<?> apiClass = Class.forName("com.gmail.filoghost.holographicdisplays.api.HologramsAPI");
            Method method = null;

            try {
                // 尝试新版API: createHologram(Plugin, Location)
                method = apiClass.getMethod("createHologram", Plugin.class, Location.class);
            } catch (NoSuchMethodException e) {
                try {
                    // 尝试旧版API: createHologram(Location)
                    method = apiClass.getMethod("createHologram", Location.class);
                } catch (NoSuchMethodException ex) {
                    // 尝试更旧版API: newInstance(World, double, double, double)
                    method = apiClass.getMethod("newInstance", World.class, double.class, double.class, double.class);
                }
            }

            if (method.getParameterCount() == 2) {
                // 新版API: createHologram(Plugin, Location)
                return method.invoke(null, plugin, location);
            } else if (method.getParameterCount() == 1) {
                // 旧版API: createHologram(Location)
                return method.invoke(null, location);
            } else if (method.getParameterCount() == 4) {
                // 更旧版API: newInstance(World, x, y, z)
                World world = location.getWorld();
                double x = location.getX();
                double y = location.getY();
                double z = location.getZ();
                return method.invoke(null, world, x, y, z);
            }

        } catch (Exception e) {
            Bukkit.getLogger().warning("无法创建全息对象: " + e.getMessage());
        }

        return null;
    }

    public static Object appendTextLine(Object hologram, String text) {
        if (!holographicDisplaysEnabled || hologram == null) {
            return null;
        }

        try {
            // 使用 Java 9+ 的反射访问模式，设置方法可访问
            Class<?> hologramClass = hologram.getClass();
            Method appendTextLineMethod = hologramClass.getMethod("appendTextLine", String.class);
            // 尝试设置方法可访问，以绕过访问限制
            appendTextLineMethod.setAccessible(true);
            return appendTextLineMethod.invoke(hologram, text);
        } catch (Exception e) {
            // 使用try-catch块捕获所有异常，包括访问权限错误
            Bukkit.getLogger().warning("无法添加文本行到全息对象: " + e.getMessage());
        }

        return null;
    }

    public static void setTouchHandler(Object hologram, Object touchHandler) {
        if (!holographicDisplaysEnabled || hologram == null || touchHandler == null) {
            return;
        }

        try {
            Class<?> hologramClass = hologram.getClass();
            Class<?> touchHandlerClass = Class.forName("com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler");
            Method setTouchHandlerMethod = hologramClass.getMethod("setTouchHandler", touchHandlerClass);
            setTouchHandlerMethod.setAccessible(true);
            setTouchHandlerMethod.invoke(hologram, touchHandler);
        } catch (Exception e) {
            Bukkit.getLogger().warning("无法设置触摸处理器: " + e.getMessage());
        }
    }

    public static void deleteHologram(Object hologram) {
        if (!holographicDisplaysEnabled || hologram == null) {
            return;
        }

        try {
            Class<?> hologramClass = hologram.getClass();
            Method deleteMethod = hologramClass.getMethod("delete");
            deleteMethod.setAccessible(true);
            deleteMethod.invoke(hologram);
        } catch (Exception e) {
            Bukkit.getLogger().warning("无法删除全息对象: " + e.getMessage());
        }
    }
}