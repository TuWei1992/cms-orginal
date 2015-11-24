package com.zving.platform.bl;

import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.platform.PlatformPlugin;

public class SystemInfoBL {

	private static String appVersion;

	/**
	 * 从插件配置文件获取产品的版本号和修订版号
	 */
	public static String getAppVersion() {
		if (appVersion == null) {
			appVersion = "Unknown";
			PluginConfig pc = PluginManager.getInstance().getPluginConfig("com.zving.product");
			if (pc == null) {
				pc = PluginManager.getInstance().getPluginConfig(PlatformPlugin.ID);
			}
			if (pc != null) {
				appVersion = pc.getVersion();
			}
		}
		return appVersion;
	}

}
