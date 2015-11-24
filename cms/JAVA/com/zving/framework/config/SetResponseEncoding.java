package com.zving.framework.config;

import com.zving.framework.Config;

/**
 * 是否统一设置http响应的字符集。
 * 值为true则开启，为false时关闭。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class SetResponseEncoding implements IApplicationConfigItem {
	public static final String ID = "SetResponseEncoding";
	public static final String KEY = "App." + ID;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Set response encoding switch";
	}

	public static boolean getValue() {
		return !"false".equals(Config.getValue(KEY));
	}

}
