package com.zving.framework.config;

import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;

/**
 * 配置日志管理器的实现类，此类必须实现com.zving.framework.utility.ILogManager接口。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class LogManagerClass implements IApplicationConfigItem {
	public static final String ID = "LogManager";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Class name which implements com.zving.framework.utility.log.ILogManager";
	}

	public static String getValue() {
		String v = Config.getValue("App." + ID);
		if (ObjectUtil.empty(v)) {
			v = "com.zving.framework.utility.log.ConsoleLogManager";
		}
		return v;
	}

}
