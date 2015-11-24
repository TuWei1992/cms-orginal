package com.zving.framework.data.command;

import java.util.HashMap;

import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.dbtype.Sybase;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;

/**
 * 修改字段的非空属性指令
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-31
 */
public class ChangeColumnMandatoryCommand implements IDBCommand {
	/**
	 * 字段名
	 */
	public String Column;
	/**
	 * 数据类型
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

	public static final String Prefix = "ChangeColumnMandatory:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		IDBType db = DBTypeService.getInstance().get(dbType);
		String sql = "alter table " + Table + " modify column " + db.maskColumnName(Column) + " "
				+ db.toSQLType(DataType, Length, Precision);
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
		Table = map.getString("Table");
		Column = map.getString("Column");
		DataType = map.getInt("DataType");
		Length = map.getInt("Length");
		Precision = map.getInt("Precision");
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		map.put("DataType", DataType);
		map.put("Column", Column);
		map.put("Length", Length);
		map.put("Precision", Precision);
		return Prefix + JSON.toJSONString(map);
	}
}
