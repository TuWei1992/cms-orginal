package com.zving.framework.data.command;

import java.util.HashMap;

import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;

/**
 * 删除索引指令
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public class DropIndexCommand implements IDBCommand {
	/**
	 * 所在数据表
	 */
	public String Table;
	/**
	 * 索引名称
	 */
	public String Name;
	public static final String Prefix = "DropIndex:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		Table = map.getString("Table");
		Name = map.getString("Name");
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		map.put("Name", Name);
		return Prefix + JSON.toJSONString(map);
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		return new String[] { "drop index " + Name };
	}
}
