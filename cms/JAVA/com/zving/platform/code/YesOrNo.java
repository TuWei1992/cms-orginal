package com.zving.platform.code;

import com.zving.framework.Config;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.platform.FixedCodeType;
import com.zving.platform.util.PlatformUtil;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-17
 */
public class YesOrNo extends FixedCodeType {

	public static final String CODETYPE = "YesOrNo";
	public static final String Yes = "Y";
	public static final String No = "N";

	public YesOrNo() {
		super(CODETYPE, "@{Platform.YesOrNo}", false, false);
		addFixedItem(Yes, "@{Platform.Yes}", getYesIcon());
		addFixedItem(No, "@{Platform.No}", getNoIcon());
	}

	public static boolean isYes(String str) {
		return Yes.equals(str);
	}

	public static boolean isNo(String str) {
		return !isYes(str);
	}

	public static String getYesIcon() {
		return Config.getContextPath() + "icons/extra/yes.gif";
	}

	public static String getNoIcon() {
		return Config.getContextPath() + "icons/extra/no.gif";
	}

	public static void decodeYesOrNoIcon(DataTable dt, String column) {
		decodeYesOrNoIcon(dt, column, true);
	}

	public static void decodeYesOrNoIcon(DataTable dt, String column, boolean showYesIconOnly) {
		String newColumnName = column + "Icon";
		if (!dt.containsColumn(newColumnName)) {
			dt.insertColumn(newColumnName);
		}
		for (DataRow dr : dt) {
			String v = dr.getString(column);
			if (isYes(v) || "true".equals(v)) {
				dr.set(newColumnName, "<img src='" + getYesIcon() + "' />");
			}
			if (!showYesIconOnly) {
				if (isNo(v) || "no".equals(v)) {
					dr.set(newColumnName, "<img src='" + getNoIcon() + "' />");
				}
			}
		}
	}

	public static String getName(String code) {
		return PlatformUtil.getCodeMap("YesOrNo").getString(code);
	}
}
