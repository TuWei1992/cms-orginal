package com.zving.framework.config;

import com.zving.framework.Config;

/**
 * 配置后台用户登录页面相对于应用根目录的地址。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class LoginMethod implements IApplicationConfigItem {
	public static final String ID = "LoginMethod";
	private static String[] methods;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Backend login method";
	}

	public static String getValue() {
		return Config.getValue("App." + ID);
	}

	public static boolean isLoginMethod(String method) {
		if (methods == null && getValue() != null) {
			methods = getValue().split("\\,");
		}
		if (methods != null && methods.length > 0) {
			for (String method2 : methods) {
				if (method.equals(method2)) {
					return true;
				}
			}
		}
		return false;
	}
}
