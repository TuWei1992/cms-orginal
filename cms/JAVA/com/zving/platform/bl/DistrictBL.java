package com.zving.platform.bl;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.utility.StringUtil;

/*
 * @Author 王育春
 * @Date 2011-1-18
 * @Mail wyuch@zving.com
 */
public class DistrictBL {

	static Mapx<String, Object> codeMap;

	static Mapx<String, Object> nameMap;

	static DataTable table;

	private static void initCache() {
		if (codeMap == null) {
			Q q = new Q().select("Name", "Code").from("ZDDistrict").where().in("TreeLevel", 1, 2, 3).orderby("TreeLevel,Code Desc");
			table = q.fetch();
			codeMap = table.toMapx(0, 1);
			nameMap = table.toMapx(1, 0);
		}
	}

	public static String getDistrictName(String code) {
		initCache();
		if (StringUtil.isEmpty(code)) {
			return "未知";
		}
		String district = null;
		if (code.startsWith("00")) {
			district = nameMap.getString(code);
		} else {
			String prov = nameMap.getString(code.substring(0, 2) + "0000");
			if (StringUtil.isNotEmpty(prov)) {
				if (prov.startsWith("黑龙江") || prov.startsWith("内蒙古")) {
					prov = prov.substring(0, 3);
				} else {
					prov = prov.substring(0, 2);
				}
				if (code.endsWith("0000")) {
					district = prov;
				} else {
					String city = nameMap.getString(code);
					if (city == null) {
						city = nameMap.getString(code.substring(0, 4) + "00");
					}
					district = prov + (city == null ? "" : city);
				}
			}
		}
		if (StringUtil.isEmpty(district)) {
			district = "未知";
		}
		return district;
	}

}
