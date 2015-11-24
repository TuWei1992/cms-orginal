package com.zving.framework.data.command;

import java.util.HashMap;

import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;

/**
 * 重命名字段指令
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-28
 */
public class RenameColumnCommand implements IDBCommand {
	/**
	 * 要重命名的字段名称
	 */
	public String Column;
	/**
	 * 新的字段名称
	 */
	public String NewColumn;
	/**
	 * 字段数据类型
	 */
	public int DataType;
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
	/**
	 * 所在数据表
	 */
	public String Table;
	public static final String Prefix = "RenameColumn:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		IDBType db = DBTypeService.getInstance().get(dbType);
		return new String[] { "alter table " + Table + " rename column " + db.maskColumnName(Column) + " to "
				+ db.maskColumnName(NewColumn) };
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		Table = map.getString("Table");
		Column = map.getString("Column");
		NewColumn = map.getString("NewColumn");
		DataType = map.getInt("DataType");
		Length = map.getInt("Length");
		Precision = map.getInt("Precision");
		Mandatory = "true".equals(map.getString("Mandatory"));
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		map.put("Column", Column);
		map.put("NewColumn", NewColumn);
		map.put("DataType", DataType);
		map.put("Length", Length);
		map.put("Precision", Precision);
		map.put("Mandatory", Mandatory);
		return Prefix + JSON.toJSONString(map);
	}
}
