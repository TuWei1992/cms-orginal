package com.zving.framework.config;

import com.zving.framework.Config;
import com.zving.framework.utility.NumberUtil;

/**
 * 最大文件上传大小，以字节为单位。<br>
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-15
 */
public class UploadMaxSize implements IApplicationConfigItem {
	public static final String ID = "UploadMaxSize";
	public static final int DEFAULT = 2 * 1024 * 1024 * 1024;
	private static int max = -1;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Maxiumn file size of upload";
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
