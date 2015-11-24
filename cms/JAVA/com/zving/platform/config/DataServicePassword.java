package com.zving.platform.config;

import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;
import com.zving.platform.PlatformPlugin;
import com.zving.platform.code.ControlType;
import com.zving.platform.code.DataType;

/**
 * DreamWeaver插件数据接入认证码
 * 
 * @author ZVING-LWY
 */
public class DataServicePassword extends FixedConfigItem {
	public static final String ID = "DataServicePassword";

	public DataServicePassword() {
		super(ID, DataType.ShortText, ControlType.Text, "@{Platform.Config.DataServicePassword}", PlatformPlugin.ID);
	}

	public static String getValue() {
		String v = Config.getValue(ID);
		if (ObjectUtil.empty(v)) {
			v = "admin";
		}
		return v;
	}
}
