package com.zving.cxdata.config;

import com.zving.cxdata.CXDataPlugin;
import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;

public class CheckContentBeforeWriteFlag extends FixedConfigItem {
	public static final String ID = "CheckContentBeforeWriteFlag";

	public CheckContentBeforeWriteFlag() {
		super(ID, "ShortText", "Radio", "生成文件检查内容是否相等", CXDataPlugin.ID);
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
	
	public static boolean checkFlag() {
		return "Y".equals(getValue());
	}
	
}
