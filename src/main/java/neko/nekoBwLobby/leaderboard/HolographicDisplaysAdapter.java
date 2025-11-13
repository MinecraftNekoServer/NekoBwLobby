package neko.nekoBwLobby.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class HolographicDisplaysAdapter {
    
    private static final String HOLOGRAPHIC_DISPLAYS_PLUGIN_NAME = "HolographicDisplays";
    private static final String HOLOGRAMS_API_CLASS = "com.gmail.filoghost.holographicdisplays.api.HologramsAPI";
    private static final String HOLOGRAM_CLASS = "com.gmail.filoghost.holographicdisplays.api.Hologram";
    private static final String TOUCH_HANDLER_CLASS = "com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler";
    
    private static Boolean isPluginAvailable = null;
    private static Method createHologramMethod = null;
    private static Method appendTextLineMethod = null;
    private static Method setTouchHandlerMethod = null;
    private static Method deleteMethod = null;
    
    static {
        initializeAPI();
    }
    
    private static void initializeAPI() {
        try {
            // 检查插件是否存在
            if (Bukkit.getPluginManager().getPlugin(HOLOGRAPHIC_DISPLAYS_PLUGIN_NAME) == null) {
                isPluginAvailable = false;
                Bukkit.getLogger().warning("HolographicDisplays插件未找到");
                return;
            }
            
            Class<?> hologramsAPIClass = Class.forName(HOLOGRAMS_API_CLASS);
            
            // 尝试获取createHologram方法 (新版API)
            try {
                createHologramMethod = hologramsAPIClass.getMethod("createHologram", Plugin.class, Location.class);
            } catch (NoSuchMethodException e) {
                // 尝试旧版API
                try {
                    createHologramMethod = hologramsAPIClass.getMethod("createHologram", Location.class);
                } catch (NoSuchMethodException ex) {
                    // 尝试更旧版API
                    try {
                        createHologramMethod = hologramsAPIClass.getMethod("newInstance", World.class, double.class, double.class, double.class);
                    } catch (NoSuchMethodException exc) {
                        Bukkit.getLogger().warning("无法找到HolographicDisplays API的createHologram方法");
                        isPluginAvailable = false;
                        return;
                    }
                }
            }
            
            // 获取Hologram类的方法
            Class<?> hologramClass = Class.forName(HOLOGRAM_CLASS);
            appendTextLineMethod = hologramClass.getMethod("appendTextLine", String.class);
            
            // 尝试获取setTouchHandler方法（某些版本可能不存在）
            try {
                setTouchHandlerMethod = hologramClass.getMethod("setTouchHandler", Class.forName(TOUCH_HANDLER_CLASS));
            } catch (NoSuchMethodException e) {
                Bukkit.getLogger().warning("HolographicDisplays API中未找到setTouchHandler方法，触摸功能将不可用");
                setTouchHandlerMethod = null;
            } catch (ClassNotFoundException e) {
                Bukkit.getLogger().warning("HolographicDisplays API中未找到TouchHandler类，触摸功能将不可用");
                setTouchHandlerMethod = null;
            }
            
            deleteMethod = hologramClass.getMethod("delete");
            
            isPluginAvailable = true;
            Bukkit.getLogger().info("HolographicDisplays API适配器初始化成功");
        } catch (Exception e) {
            isPluginAvailable = false;
            Bukkit.getLogger().warning("无法初始化HolographicDisplays API适配器: " + e.getMessage());
        }
    }
    
    public static boolean isHolographicDisplaysAvailable() {
        if (isPluginAvailable == null) {
            initializeAPI();
        }
        return isPluginAvailable != null && isPluginAvailable;
    }
    
    public static Object createHologram(Plugin plugin, Location location) throws Exception {
        if (!isHolographicDisplaysAvailable()) {
            throw new IllegalStateException("HolographicDisplays插件不可用");
        }
        
        if (createHologramMethod.getParameterCount() == 2) {
            // 新版API: createHologram(Plugin, Location)
            return createHologramMethod.invoke(null, plugin, location);
        } else if (createHologramMethod.getParameterCount() == 1) {
            // 旧版API: createHologram(Location)
            return createHologramMethod.invoke(null, location);
        } else if (createHologramMethod.getParameterCount() == 4) {
            // 更旧版API: newInstance(World, x, y, z)
            World world = location.getWorld();
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            return createHologramMethod.invoke(null, world, x, y, z);
        }
        
        throw new IllegalStateException("未知的API方法签名");
    }
    
    public static Object appendTextLine(Object hologram, String text) throws Exception {
        if (!isHolographicDisplaysAvailable()) {
            throw new IllegalStateException("HolographicDisplays API不可用");
        }
        return appendTextLineMethod.invoke(hologram, text);
    }
    
    public static void setTouchHandler(Object hologram, Object touchHandler) throws Exception {
        if (!isHolographicDisplaysAvailable()) {
            throw new IllegalStateException("HolographicDisplays API不可用");
        }
        // 只有当setTouchHandler方法存在时才调用
        if (setTouchHandlerMethod != null) {
            setTouchHandlerMethod.invoke(hologram, touchHandler);
        } else {
            Bukkit.getLogger().warning("setTouchHandler方法不可用，跳过触摸处理器设置");
        }
    }
    
    public static void deleteHologram(Object hologram) throws Exception {
        if (!isHolographicDisplaysAvailable()) {
            throw new IllegalStateException("HolographicDisplays API不可用");
        }
        deleteMethod.invoke(hologram);
    }
}