package com.zving.framework.data.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.dbtype.Sybase;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONArray;
import com.zving.framework.json.JSONObject;
import com.zving.framework.orm.DAOColumn;

/**
 * 创建数据表指令
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public class CreateTableCommand implements IDBCommand {
	/**
	 * 要创建的数据表
	 */
	public String Table;
	/**
	 * 表中包含的字段
	 */
	public List<DAOColumn> Columns;

	public static final String Prefix = "CreateTable:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public void parse(String ddl) {
		ddl = ddl.substring(Prefix.length());
		JSONObject map = (JSONObject) JSON.parse(ddl);
		Table = map.getString("Table");
		JSONArray cs = (JSONArray) map.get("Columns");
		Columns = new ArrayList<DAOColumn>();
		for (Object obj : cs) {
			JSONArray arr = (JSONArray) obj;
			DAOColumn sc = new DAOColumn(arr.get(0).toString(), Integer.parseInt(arr.get(1).toString()), Integer.parseInt(arr.get(2)
					.toString()), Integer.parseInt(arr.get(3).toString()), "true".equals(arr.get(4).toString()), "true".equals(arr.get(5)
					.toString()));
			Columns.add(sc);
		}
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		Object[][] columns = new Object[Columns.size()][6];
		int i = 0;
		for (DAOColumn sc : Columns) {
			Object[] arr = new Object[] { sc.getColumnName(), sc.getColumnType(), sc.getLength(), sc.getPrecision(), sc.isMandatory(),
					sc.isPrimaryKey() };
			columns[i++] = arr;
		}
		map.put("Columns", columns);
		return Prefix + JSON.toJSONString(map);
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		IDBType db = DBTypeService.getInstance().get(dbType);
		StringBuilder sb = new StringBuilder();
		sb.append("create table " + Table + "(\n");
		StringBuilder ksb = new StringBuilder();
		IDBType t = DBTypeService.getInstance().get(dbType);
		for (int i = 0; i < Columns.size(); i++) {
			DAOColumn sc = Columns.get(i);
			if (i != 0) {
				sb.append(",\n");
			}
			sb.append("\t" + db.maskColumnName(sc.getColumnName()) + " ");
			String sqlType = t.toSQLType(sc.getColumnType(), sc.getLength(), sc.getPrecision());
			sb.append(sqlType);
			if (sc.isMandatory()) {
				sb.append(" not null");
			} else {
				if (dbType.equalsIgnoreCase(Sybase.ID)) {
					sb.append(" null");// 必须写明，不写明的话sybase默认为非空
				}
			}
			if (sc.isPrimaryKey()) {
				if (ksb.length() == 0) {
					String pkName = Table;
					if (pkName.length() > 15) {
						pkName = pkName.substring(0, 15);
					}
					ksb.append("\t" + t.getPKNameFragment(Table) + " (");
				} else {
					ksb.append(",");
				}
				ksb.append(sc.getColumnName());
			}
		}
		if (ksb.length() != 0) {
			ksb.append(")");
			sb.append(",\n" + ksb);
		}
		sb.append("\n)");
		return new String[] { sb.toString() };
	}
}
