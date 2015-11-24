package com.zving.platform.log.menu;

import com.zving.platform.ILogMenu;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
public class UserLogMenu implements ILogMenu {

	@Override
	public String getExtendItemID() {
		return "UserLog";
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.UserLogMenu}";
	}

	@Override
	public String getDetailURL() {
		return "logs/userLog.zhtml";
	}

	@Override
	public String getGroupID() {
		return SystemGroup.ID;
	}

}
