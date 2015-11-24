package com.zving.platform.log.menu;

import com.zving.framework.i18n.Lang;
import com.zving.platform.ILogMenu;

/**
 * @author 张金灿
 * @mail zjc@zving.com
 * @date 2014-5-16
 */
public class DownLogMenu implements ILogMenu {

	@Override
	public String getExtendItemID() {
		return "DownLog";
	}

	@Override
	public String getExtendItemName() {
		return Lang.get("Logs.DownLog");
	}

	@Override
	public String getDetailURL() {
		return "logs/downSystemLog.zhtml";
	}

	@Override
	public String getGroupID() {
		return SystemGroup.ID;
	}

}
