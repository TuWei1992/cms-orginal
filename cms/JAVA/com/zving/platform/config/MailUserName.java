package com.zving.platform.config;

import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;
import com.zving.platform.PlatformPlugin;
import com.zving.platform.code.ControlType;
import com.zving.platform.code.DataType;

public class MailUserName extends FixedConfigItem {
	public static final String ID = "Platform.Mail.UserName";

	public MailUserName() {
		super(ID, DataType.ShortText, ControlType.Text, "@{Platform.Mail.UserName}", PlatformPlugin.ID);
	}

	public static String getValue() {
		String v = Config.getValue(ID);
		if (ObjectUtil.empty(v)) {
			v = null;
		}
		return v;
	}
}
