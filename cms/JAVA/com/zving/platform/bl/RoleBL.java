package com.zving.platform.bl;

import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-9-14
 */
public class RoleBL {
	public static String getAdminRoleCode() {
		String role = Config.getValue("System.AdminRoleCode");
		if (ObjectUtil.empty(role)) {
			role = "admin";
		}
		return role;
	}
}
