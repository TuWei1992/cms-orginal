package com.zving.cxdata.config;

import com.zving.cxdata.CXDataPlugin;
import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;

public class RefreshSessionFlag extends FixedConfigItem {
	public static final String ID = "RefreshSessionFlag";

	public RefreshSessionFlag() {
		super(ID, "ShortText", "Radio", "是否刷新Session", CXDataPlugin.ID);
		super.addOption("Y", "@{Common.Yes}");
		super.addOption("N", "@{Common.No}");
	}

	public static String getValue() {
		String v = Config.getValue(ID);
		if (ObjectUtil.empty(v)) {
			v = null;
		}
		return v;
	}
}
