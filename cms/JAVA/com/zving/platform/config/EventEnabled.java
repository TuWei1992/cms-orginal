package com.zving.platform.config;

import com.zving.framework.Config;
import com.zving.framework.config.IApplicationConfigItem;

public class EventEnabled implements IApplicationConfigItem {

	public static final String ID = "Event.Enabled";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "MessageEvent Queue work enabled";
	}

	public static String getValue() {
		return Config.getValue("App." + ID);
	}
}
