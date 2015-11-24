package com.zving.framework.config;

import com.zving.framework.Config;
import com.zving.framework.utility.Primitives;

/**
 * 转义符是否自动转义，默认为false<br>
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class ExpressionAutoEscaping implements IApplicationConfigItem {
	public static final String ID = "ExpressionAutoEscaping";
	public static final boolean DEFAULT = false;
	private static boolean loaded = false;
	private static boolean value = DEFAULT;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Expression value auto escaping";
	}

	public static boolean getValue() {
		if (!loaded) {
			String str = Config.getValue("App." + ID);
			value = Primitives.getBoolean(str);
			loaded = true;
		}
		return value;
	}
}
