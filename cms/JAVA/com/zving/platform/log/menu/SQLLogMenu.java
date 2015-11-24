package com.zving.platform.log.menu;

import com.zving.platform.ILogMenu;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
public class SQLLogMenu implements ILogMenu {

	@Override
	public String getExtendItemID() {
		return "SQLLog";
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.SQLLogMenu}";
	}

	@Override
	public String getDetailURL() {
		return "logs/SQLLog.zhtml";
	}

	@Override
	public String getGroupID() {
		return SystemGroup.ID;
	}

}
