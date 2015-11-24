package com.zving.framework.config;

import com.zving.framework.Config;
import com.zving.framework.utility.NumberUtil;

/**
 * 每页记录数的最大值，默认1000。<br>
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class MaxPageSize implements IApplicationConfigItem {
	public static final String ID = "MaxPageSize";
	public static final int DEFAULT = 1000;
	private static int max = -1;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Maxiumn page size";
	}

	public static int getValue() {
		if (max < 0) {
			String str = Config.getValue("App." + ID);
			if (NumberUtil.isInt(str)) {
				max = Integer.parseInt(str);
			} else {
				max = DEFAULT;
			}
		}
		return max;
	}
}
