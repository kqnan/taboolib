package taboolib.platform;

import net.md_5.bungee.BungeeCord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.classloader.IsolatedClassLoader;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda", "DuplicatedCode"})
@PlatformSide(Platform.BUNGEE)
public class BungeePlugin extends net.md_5.bungee.api.plugin.Plugin {

    @Nullable
    private static Plugin pluginInstance;
    private static BungeePlugin instance;
    private static Class<?> delegateClass;
    private static Object delegateObject;

    static {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                IsolatedClassLoader loader = new IsolatedClassLoader(
                        new URL[]{BungeePlugin.class.getProtectionDomain().getCodeSource().getLocation()},
                        BungeePlugin.class.getClassLoader()
                );
                delegateClass = Class.forName("taboolib.platform.BungeePluginDelegate", true, loader);
                delegateObject = delegateClass.getConstructor().newInstance();
                delegateClass.getMethod("onConst").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUNGEE);
            // 搜索 Plugin 实现
            if (TabooLibCommon.isKotlinEnvironment()) {
                pluginInstance = Project1Kt.findImplementation(Plugin.class);
            }
        }
    }

    public BungeePlugin() {
        instance = this;
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onInit").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            // 生命周期
            TabooLibCommon.lifeCycle(LifeCycle.INIT);
        }
    }

    @Override
    public void onLoad() {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onLoad").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.LOAD);
            // 再次尝试搜索 Plugin 实现
            if (pluginInstance == null) {
                pluginInstance = Project1Kt.findImplementation(Plugin.class);
            }
            // 调用 Plugin 实现的 onLoad() 方法
            if (pluginInstance != null && !TabooLibCommon.isStopped()) {
                pluginInstance.onLoad();
            }
        }
    }

    @Override
    public void onEnable() {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onEnable").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
            // 判断插件是否关闭
            if (!TabooLibCommon.isStopped()) {
                // 调用 onEnable() 方法
                if (pluginInstance != null) {
                    pluginInstance.onEnable();
                }
                // 启动调度器
                try {
                    ExecutorKt.startExecutor();
                } catch (NoClassDefFoundError ignored) {
                }
            }
            // 再次判断插件是否关闭
            if (!TabooLibCommon.isStopped()) {
                // 创建调度器，执行 onActive() 方法
                BungeeCord.getInstance().getScheduler().schedule(this, new Runnable() {
                    @Override
                    public void run() {
                        TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                        if (pluginInstance != null) {
                            pluginInstance.onActive();
                        }
                    }
                }, 0, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void onDisable() {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onDisable").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
            // 在插件未关闭的前提下，执行 onDisable() 方法
            if (pluginInstance != null && !TabooLibCommon.isStopped()) {
                pluginInstance.onDisable();
            }
        }
    }

    @NotNull
    public static BungeePlugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }
}
