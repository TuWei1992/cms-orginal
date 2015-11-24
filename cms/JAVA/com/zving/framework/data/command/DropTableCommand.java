package com.zving.framework.data.command;

import java.util.HashMap;

import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;

/**
 * 删除数据表指令
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public class DropTableCommand implements IDBCommand {
	/**
	 * 要删除的数据表
	 */
	public String Table;

	public static final String Prefix = "DropTable:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		Table = map.getString("Table");
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		return Prefix + JSON.toJSONString(map);
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		return new String[] { "drop table " + Table };
	}
}
