package com.zving.platform.config;

import com.zving.framework.Config;
import com.zving.framework.config.IApplicationConfigItem;

public class EventUrl implements IApplicationConfigItem {

	public static final String ID = "Event.Url";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "MessageEvent queue post url";
	}

	public static String getValue() {
		return Config.getValue("App." + ID);
	}
}
