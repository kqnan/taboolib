package taboolib.platform;

import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.lang.reflect.Field;

import static taboolib.platform.VelocityPlugin.getPluginInstance;

@SuppressWarnings({"Convert2Lambda", "DuplicatedCode"})
public class VelocityPluginDelegate {

	private final Field pluginInstance;

	public VelocityPluginDelegate() throws ClassNotFoundException, NoSuchFieldException {
		this.pluginInstance = Class.forName("taboolib.platform.VelocityPlugin").getDeclaredField("pluginInstance");
		this.pluginInstance.setAccessible(true);
	}

	public void onConst() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.VELOCITY);
		if (TabooLibCommon.isKotlinEnvironment()) {
			pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
		}
	}

	public void onInit() {
		// 生命周期
		TabooLibCommon.lifeCycle(LifeCycle.INIT);
	}

	public void onLoad() throws IllegalAccessException {
		if (!TabooLibCommon.isStopped()) {
			TabooLibCommon.lifeCycle(LifeCycle.LOAD);
			if (getPluginInstance() == null) {
				pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
			}
			if (getPluginInstance() != null) {
				getPluginInstance().onLoad();
			}
		}
		if (!TabooLibCommon.isStopped()) {
			TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
			if (getPluginInstance() != null) {
				getPluginInstance().onEnable();
			}
			try {
				ExecutorKt.startExecutor();
			} catch (NoClassDefFoundError ignored) {
			}
		}
		if (!TabooLibCommon.isStopped()) {
			VelocityPlugin.getInstance().getServer().getScheduler().buildTask(VelocityPlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
					if (getPluginInstance() != null) {
						getPluginInstance().onActive();
					}
				}
			}).schedule();
		}
	}

	public void onDisable() {
		TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
		// 在插件未关闭的前提下，执行 onDisable() 方法
		if (getPluginInstance() != null && !TabooLibCommon.isStopped()) {
			getPluginInstance().onDisable();
		}
	}
}
