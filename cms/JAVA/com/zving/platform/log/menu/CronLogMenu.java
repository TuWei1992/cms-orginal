package com.zving.platform.log.menu;

import com.zving.platform.ILogMenu;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
public class CronLogMenu implements ILogMenu {

	@Override
	public String getExtendItemID() {
		return "CronLog";
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.CronLogMenu}";
	}

	@Override
	public String getDetailURL() {
		return "logs/cronLog.zhtml";
	}

	@Override
	public String getGroupID() {
		return SystemGroup.ID;
	}

}
