package com.zving.framework.data.command;

import java.util.HashMap;

import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.dbtype.Sybase;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;

/**
 * 添加字段指令
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public class AddColumnCommand implements IDBCommand {
	/**
	 * 字段名
	 */
	public String Column;
	/**
	 * 数据类型
	 */
	public int DataType;
	/**
	 * 所在数据表
	 */
	public String Table;
	/**
	 * 字段长度
	 */
	public int Length;
	/**
	 * 字段精度
	 */
	public int Precision;
	/**
	 * 是否必填
	 */
	public boolean Mandatory;

	public static final String Prefix = "AddColumn:";

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		IDBType db = DBTypeService.getInstance().get(dbType);
		String sql = "alter table " + Table + " add column " + db.maskColumnName(Column) + " " + db.toSQLType(DataType, Length, Precision);
		if (Mandatory) {
			sql += " not null";
		} else if (dbType.equalsIgnoreCase(Sybase.ID)) {
			sql += " null";
		}
		return new String[] { sql };
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		Column = map.getString("Column");
		DataType = map.getInt("DataType");
		Table = map.getString("Table");
		Length = map.getInt("Length");
		Precision = map.getInt("Precision");
		Mandatory = map.getBoolean("Mandatory");
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Column", Column);
		map.put("DataType", DataType);
		map.put("Table", Table);
		map.put("Length", Length);
		map.put("Precision", Precision);
		map.put("Mandatory", Mandatory);
		return Prefix + JSON.toJSONString(map);
	}

	@Override
	public String getPrefix() {
		return Prefix;
	}
}
