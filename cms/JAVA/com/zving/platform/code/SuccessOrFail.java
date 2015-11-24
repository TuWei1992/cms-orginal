package com.zving.platform.code;

import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.platform.FixedCodeType;
import com.zving.platform.util.PlatformUtil;

/**
 * 成功/失败
 * 
 * @author 李伟仪
 * @mail lwy@zving.com
 * @date 2013-7-24
 */
public class SuccessOrFail extends FixedCodeType {

	public static final String CODETYPE = "SuccessOrFail";
	public static final String SUCCESS = "S";
	public static final String FAIL = "F";

	public SuccessOrFail() {
		super(CODETYPE, "@{Platform.SuccessOrFailed}", false, false);
		addFixedItem(SUCCESS, "@{Platform.Success}", null);
		addFixedItem(FAIL, "@{Platform.Failed}", null);
	}

	public static boolean isSuccess(String str) {
		return SUCCESS.equals(str);
	}

	public static boolean isFail(String str) {
		return !isSuccess(str);
	}

	public static void decode(DataTable dt, String column) {
		String newColumnName = column + "Name";
		if (!dt.containsColumn(newColumnName)) {
			dt.insertColumn(newColumnName);
		}
		for (DataRow dr : dt) {
			String v = dr.getString(column);
			if (isSuccess(v)) {
				dr.set(newColumnName, "@{Platform.Success}");
			} else {
				dr.set(newColumnName, "@{Platform.Failed}");
			}
		}
	}

	public static String getName(String code) {
		return PlatformUtil.getCodeMap(CODETYPE).getString(code);
	}
}
