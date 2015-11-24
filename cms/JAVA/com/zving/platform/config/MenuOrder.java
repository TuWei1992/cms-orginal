package com.zving.platform.config;

import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;
import com.zving.platform.PlatformPlugin;
import com.zving.platform.code.ControlType;
import com.zving.platform.code.DataType;

public class MenuOrder extends FixedConfigItem {
	public static final String ID = "Platform.MenuOrder";

	public MenuOrder() {
		super(ID, DataType.ShortText, ControlType.TextArea, "@{Platform.config.MenuOrder}", PlatformPlugin.ID);
	}

	public static String getValue() {
		String v = Config.getValue(ID);
		if (ObjectUtil.empty(v)) {
			v = null;
		}
		return v;
	}
}
