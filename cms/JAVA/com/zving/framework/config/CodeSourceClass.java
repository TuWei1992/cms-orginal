package com.zving.framework.config;

import com.zving.framework.Config;

/**
 * 配置代码源。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class CodeSourceClass implements IApplicationConfigItem {
	public static final String ID = "CodeSource";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "CodeSource's subclass which provider code data";
	}

	public static String getValue() {
		return Config.getValue("App." + ID);
	}

}
