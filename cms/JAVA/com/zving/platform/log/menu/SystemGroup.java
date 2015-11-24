package com.zving.platform.log.menu;

import com.zving.platform.ILogMenuGroup;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
public class SystemGroup implements ILogMenuGroup {
	public static final String ID = "System";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.SystemLog}";
	}

}
