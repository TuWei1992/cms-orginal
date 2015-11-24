package com.zving.framework.data.command;

import java.util.HashMap;

import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;

/**
 * 自由SQL指令
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public class SQLCommand implements IDBCommand {// NO_UCD
	/**
	 * 指令对应的SQL
	 */
	public String SQL;

	public static final String Prefix = "SQL:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		return new String[] { SQL };
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		SQL = map.getString("SQL");
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("SQL", SQL);
		return Prefix + JSON.toJSONString(map);
	}

}
