package com.zving.platform.log.menu;

import com.zving.platform.ILogMenu;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
public class SecurityLogMenu implements ILogMenu {

	@Override
	public String getExtendItemID() {
		return "SecurityLog";
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.SecurityLogMenu}";
	}

	@Override
	public String getDetailURL() {
		return "logs/securityLog.zhtml";
	}

	@Override
	public String getGroupID() {
		return SystemGroup.ID;
	}

}
