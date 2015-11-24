package com.zving.platform.log.menu;

import com.zving.platform.ILogMenu;

public class UserLoginLogMenu implements ILogMenu {

	@Override
	public String getExtendItemID() {
		return "UserLoginLog";
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.UserLoginLog}";
	}

	@Override
	public String getDetailURL() {
		return "logs/userLoginLog.zhtml";
	}

	@Override
	public String getGroupID() {
		return SystemGroup.ID;
	}

}
