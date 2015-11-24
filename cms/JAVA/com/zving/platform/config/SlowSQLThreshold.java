package com.zving.platform.config;

import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;
import com.zving.platform.PlatformPlugin;
import com.zving.platform.code.ControlType;
import com.zving.platform.code.DataType;

/**
 * 慢SQL阈值，SQL执行时间高于此值则判定为慢SQL。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class SlowSQLThreshold extends FixedConfigItem {
	public static final String ID = "Platform.SlowSQLThreshold";

	public SlowSQLThreshold() {
		super(ID, DataType.Long, ControlType.Text, "@{Platform.config.SlowSQLThreshold}", PlatformPlugin.ID);
	}

	public static int getValue() {
		String v = Config.getValue(ID);
		if (ObjectUtil.empty(v)) {
			return 1000;
		} else {
			return Integer.parseInt(v);
		}
	}
}
