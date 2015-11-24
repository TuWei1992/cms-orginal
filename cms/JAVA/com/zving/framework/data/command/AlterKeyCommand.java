package com.zving.framework.data.command;

import java.util.HashMap;
import java.util.List;

import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.dbtype.MSSQL;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONArray;
import com.zving.framework.json.JSONObject;
import com.zving.framework.utility.ObjectUtil;

/**
 * 修改主键指令
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public class AlterKeyCommand implements IDBCommand {
	/**
	 * 所在数据表
	 */
	public String Table;
	/**
	 * 主键包含的字段
	 */
	public List<String> Columns;

	public static final String Prefix = "AlterKey:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		Table = map.getString("Table");
		Columns = ObjectUtil.toStringList((JSONArray) map.get("Columns"));
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		map.put("Columns", Columns);
		return Prefix + JSON.toJSONString(map);
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		String sql1 = null;
		if (MSSQL.ID.equals(dbType)) {
			sql1 = "alter table " + Table + " drop constraint PK_" + Table;
		} else {
			sql1 = "alter table " + Table + " drop primary key";
		}
		IDBType db = DBTypeService.getInstance().get(dbType);
		StringBuilder sb = new StringBuilder();
		sb.append("alter table ").append(Table).append(" add ").append(db.getPKNameFragment(Table)).append(" (");
		for (int i = 0; i < Columns.size(); i++) {
			if (i != 0) {
				sb.append(',');
			}
			sb.append(db.maskColumnName(Columns.get(i)));
		}
		sb.append(')');
		return new String[] { sql1, sb.toString() };
	}
}
