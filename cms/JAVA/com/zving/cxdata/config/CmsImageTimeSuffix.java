package com.zving.cxdata.config;

import com.zving.cxdata.CXDataPlugin;
import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;

public class CmsImageTimeSuffix extends FixedConfigItem {
	public static final String ID = "CmsImageTimeSuffix";

	public CmsImageTimeSuffix() {
		super(ID, "ShortText", "Radio", "CmsImage图片后缀", CXDataPlugin.ID);
		super.addOption("0", "无");
		super.addOption("1", "CurrentTimeMillion");
		super.addOption("2", "FileModifyTime");
	}

	public static String getValue() {
		String v = Config.getValue(ID);
		if (ObjectUtil.empty(v)) {
			v = null;
		}
		return v;
	}
	
	public static boolean isCurrentTimeMillion() {
		return "1".equals(getValue());
	}
	
	public static boolean isFileModifyTime() {
		return "2".equals(getValue());
	}
	
}
