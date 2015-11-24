package com.zving.platform.code;

import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.platform.FixedCodeType;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-17
 */
public class Enable extends FixedCodeType {
	public static final String Enable = "Y";
	public static final String Disable = "N";

	public Enable() {
		super("Enable", "@{Platform.EnableOrDisable}", false, false);
		addFixedItem(Enable, "@{Platform.Enabled}", null);
		addFixedItem(Disable, "@{Platform.Disabled}", null);
	}

	public static boolean isEnable(String str) {
		return Enable.equals(str);
	}

	public static boolean isDisable(String str) {
		return Disable.equals(str);
	}

	public static void decode(DataTable dt, String column) {
		String newColumnName = column + "Name";
		if (!dt.containsColumn(newColumnName)) {
			dt.insertColumn(newColumnName);
		}
		for (DataRow dr : dt) {
			String v = dr.getString(column);
			if (isEnable(v)) {
				dr.set(newColumnName, "@{Platform.Enabled}");
			} else if (isDisable(v)) {
				dr.set(newColumnName, "@{Platform.Disabled}");
			}
		}
	}
}
