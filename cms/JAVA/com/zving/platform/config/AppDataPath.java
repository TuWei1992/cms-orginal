package com.zving.platform.config;

import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;
import com.zving.platform.PlatformPlugin;
import com.zving.platform.code.ControlType;
import com.zving.platform.code.DataType;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-24
 */
public class AppDataPath extends FixedConfigItem {
	public static final String ID = "Platform.AppDataPath";

	public AppDataPath() {
		super(ID, DataType.ShortText, ControlType.Text, "@{Platform.config.AppDataPath}", PlatformPlugin.ID);
	}

	public static String getValue() {
		String v = Config.getValue(ID);
		if (ObjectUtil.empty(v)) {
			v = Config.getContextRealPath() + "WEB-INF/data/";
		} else {
			v = FixedConfigItem.replacePathHolder(v);
		}
		if (!v.endsWith("/")) {
			v = v + "/";
		}
		return v;
	}
}
