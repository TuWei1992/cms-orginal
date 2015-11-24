package com.zving.framework.config;

import com.zving.framework.Config;

/**
 * 配置哪些包不进行注解扫描。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class ExcludeClassScan implements IApplicationConfigItem {
	public static final String ID = "ExcludeClassScan";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Class or package exclude in annotation scanning";
	}

	public static String getValue() {
		return Config.getValue("App." + ID);
	}

}
