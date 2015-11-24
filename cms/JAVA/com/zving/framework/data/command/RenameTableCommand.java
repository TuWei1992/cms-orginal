package com.zving.framework.data.command;

import java.util.HashMap;

import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;

/**
 * 重命名数据表指令
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-26
 */
public class RenameTableCommand implements IDBCommand {
	public static final String Prefix = "RenameTable:";
	/**
	 * 要重命名的数据表名
	 */
	public String Table;
	/**
	 * 新的数据表名
	 */
	public String NewTable;

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		return new String[] { "rename table " + Table + " to " + NewTable };
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		Table = map.getString("Table");
		NewTable = map.getString("NewTable");
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		map.put("NewTable", NewTable);
		return Prefix + JSON.toJSONString(map);
	}
}
