/**
 * 
 */
package com.zving.platform.util;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.ui.CodeSource;
import com.zving.framework.utility.StringUtil;

/**
 * 代码来源实现类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2006-10-5
 */
public class PlatformCodeSource extends CodeSource {

	@Override
	public DataTable getCodeData(String codeType, Mapx<String, Object> params) {
		DataTable dt = null;
		String conditionField = (String) params.get("ConditionField");
		String conditionValue = (String) params.get("ConditionValue");
		if ("District".equals(codeType)) {
			Q qb = new Q("select code,name from ZDDistrict where " + conditionField + "=?", conditionValue);
			String parentCode = (String) params.get("ParentCode");
			if (StringUtil.isNotEmpty(parentCode)) {
				qb.append(" and Code like ?");

				if (parentCode.startsWith("11") || parentCode.startsWith("12") || parentCode.startsWith("31")
						|| parentCode.startsWith("50")) {
					qb.add(parentCode.substring(0, 2) + "%");
					qb.append(" and TreeLevel=3");
				} else if (parentCode.endsWith("0000")) {
					qb.add(parentCode.substring(0, 2) + "%");
					qb.append(" and TreeLevel=2");
				} else if (parentCode.endsWith("00")) {
					qb.add(parentCode.substring(0, 4) + "%");
					qb.append(" and TreeLevel=3");
				} else {
					qb.add("#");// 返回空列表
				}
			} else if (conditionField.equals("1")) {
				return new DataTable();
			}
			dt = qb.fetch();
		} else if ("User".equals(codeType)) {
			Q qb = new Q("select UserName,UserName as 'Name',RealName,isBranchAdmin from ZDUser where " + conditionField + "=?",
					conditionValue);
			dt = qb.fetch();
		} else {
			Mapx<String, Object> map = PlatformUtil.getCodeMap(codeType);
			if (map != null) {
				dt = map.toDataTable();
				LangUtil.decode(dt, "Value");
			}
		}
		return dt;
	}
}
