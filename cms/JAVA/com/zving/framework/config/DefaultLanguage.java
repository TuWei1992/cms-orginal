package com.zving.framework.config;

import com.zving.framework.Config;

/**
 * 配置默认语言。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class DefaultLanguage implements IApplicationConfigItem {
	public static final String ID = "DefaultLanguage";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Backend default language";
	}

	public static String getValue() {
		return Config.getValue("App." + ID);
	}

}
