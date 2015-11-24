package com.zving.framework.data.command;

import java.util.HashMap;
import java.util.List;

import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONArray;
import com.zving.framework.json.JSONObject;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 创建索引指令
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public class CreateIndexCommand implements IDBCommand {
	/**
	 * 所在数据表
	 */
	public String Table;
	/**
	 * 索引名称
	 */
	public String Name;
	/**
	 * 索引包含的字段
	 */
	public List<String> Columns;

	public static final String Prefix = "CreateIndex:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		IDBType db = DBTypeService.getInstance().get(dbType);
		StringBuilder sb = new StringBuilder();
		sb.append("create index ");
		sb.append(Name);
		sb.append(" on ");
		sb.append(Table);
		sb.append(" (");
		boolean first = true;
		for (String column : Columns) {
			if (StringUtil.isEmpty(column)) {
				continue;
			}
			if (!first) {
				sb.append(",");
			}
			sb.append(db.maskColumnName(column));
			first = false;
		}
		sb.append(")");
		return new String[] { sb.toString() };
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		Table = map.getString("Table");
		Columns = ObjectUtil.toStringList((JSONArray) map.get("Columns"));
		Name = map.getString("Name");
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		map.put("Columns", Columns);
		map.put("Name", Name);
		return Prefix + JSON.toJSONString(map);
	}
}
