package com.zving.framework.config;

import com.zving.framework.Config;

/**
 * 配置前台会员登录页面相对于应用根目录的地址。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class MemberLoginPage implements IApplicationConfigItem {
	public static final String ID = "MemberLoginPage";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Member login URL";
	}

	public static String getValue() {
		return Config.getValue("App." + ID);
	}

}
