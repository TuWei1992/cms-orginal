package com.zving.framework.config;

import com.zving.framework.Config;

/**
 * 配置是否输出SQL语句到日志中。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class LogSQL implements IApplicationConfigItem {
	public static final String ID = "Log.SQL";
	private static ThreadLocal<Boolean> threadEnable = new ThreadLocal<Boolean>();

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Show SQL log switch";
	}

	public static boolean getValue() {
		if (threadEnable.get() != null && !threadEnable.get()) {
			return false;
		}
		return !"false".equals(Config.getValue("App." + ID));
	}

	/**
	 * 在本线程中启用SQL日志输出
	 */
	public static void enableInCurrentThread() {
		threadEnable.set(true);
	}

	/**
	 * 在本线程中停用SQL日志输出
	 */
	public static void disableInCurrentThread() {
		threadEnable.set(false);
	}
}
