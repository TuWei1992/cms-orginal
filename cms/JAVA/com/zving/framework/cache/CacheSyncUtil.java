package com.zving.framework.cache;

import com.zving.framework.config.CacheSyncEnable;
import com.zving.framework.config.CacheSynchronizerClass;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-10-28
 */
public class CacheSyncUtil {
	private static ICacheSynchronizer synchronizer = null;
	private static boolean enabled = false;
	private static boolean loaded = false;

	private static void init() {
		if (!loaded) {
			try {
				enabled = "true".equalsIgnoreCase(CacheSyncEnable.getValue());
				if (!enabled) {
					return;
				}
				String className = CacheSynchronizerClass.getValue();
				synchronizer = (ICacheSynchronizer) Class.forName(className).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				loaded = true;
			}
		}
	}

	public static boolean enabled() {
		init();
		return enabled;
	}

	public static void refresh(String providerID, String type) {
		init();
		if (enabled && synchronizer != null) {
			synchronizer.refresh(providerID, type);
		}
	}

	public static void refresh(String providerID, String type, String key) {
		init();
		if (enabled && synchronizer != null) {
			synchronizer.refresh(providerID, type, key);
		}
	}

	public static void sync() {
		init();
		if (enabled && synchronizer != null) {
			synchronizer.sync();
		}
	}
}
