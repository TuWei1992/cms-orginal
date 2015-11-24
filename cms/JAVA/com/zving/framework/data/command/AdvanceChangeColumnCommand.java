package com.zving.framework.data.command;

import java.util.HashMap;
import java.util.List;

import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONObject;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringFormat;

/**
 * 复杂字段修改指令（不包含重命名）
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public class AdvanceChangeColumnCommand implements IDBCommand {
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
	 * 原是否必填
	 */
	public boolean OldMandatory;
	/**
	 * 原字段类型
	 */
	public int OldDataType;
	/**
	 * 原字段长度
	 */
	public int OldLength;
	/**
	 * 原字段精度
	 */
	public int OldPrecision;
	/**
	 * 所在数据表
	 */
	public String Table;

	public static final String Prefix = "AdvanceChangeColumn:";

	@Override
	public String getPrefix() {
		return Prefix;
	}

	@Override
	public String[] getDefaultSQLArray(String dbType) {
		IDBType db = DBTypeService.getInstance().get(dbType);
		// 新增临时字段
		AddColumnCommand acc = new AddColumnCommand();
		acc.Table = Table;
		acc.Column = Column + "_tmp";
		acc.DataType = OldDataType;
		acc.Length = OldLength;
		acc.Precision = OldPrecision;
		acc.Mandatory = false;
		List<String> list = ObjectUtil.toList(db.toSQLArray(acc));

		// 原字段置为非空
		if (OldMandatory) {
			ChangeColumnMandatoryCommand ccm = new ChangeColumnMandatoryCommand();
			ccm.Table = Table;
			ccm.Column = Column;
			ccm.DataType = OldDataType;
			ccm.Length = OldLength;
			ccm.Precision = OldPrecision;
			ccm.Mandatory = false;
			list.addAll(ObjectUtil.toList(db.toSQLArray(ccm)));
		}

		// 复制信息到临时到字段
		list.add(StringFormat.format("update ? set ?=?,?=null", Table, Column + "_tmp", db.maskColumnName(Column),
				db.maskColumnName(Column)));

		// 删除原字段
		DropColumnCommand dcc = new DropColumnCommand();
		dcc.Table = Table;
		dcc.Column = Column;
		list.addAll(ObjectUtil.toList(db.toSQLArray(dcc)));

		// 重建新字段
		acc = new AddColumnCommand();
		acc.Table = Table;
		acc.Column = Column;
		acc.DataType = DataType;
		acc.Length = Length;
		acc.Precision = Precision;
		acc.Mandatory = false;
		list.addAll(ObjectUtil.toList(db.toSQLArray(acc)));

		// 还原数据
		list.add(StringFormat.format("update ? set ?=?,?=null", Table, db.maskColumnName(Column), Column + "_tmp", Column + "_tmp"));

		// 删除临时字段
		dcc = new DropColumnCommand();
		dcc.Table = Table;
		dcc.Column = Column + "_tmp";
		list.addAll(ObjectUtil.toList(db.toSQLArray(dcc)));

		String[] arr = new String[list.size()];
		return list.toArray(arr);
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
		Mandatory = "true".equals(map.getString("Mandatory"));
		OldDataType = map.getInt("OldDataType");
		OldLength = map.getInt("OldLength");
		OldPrecision = map.getInt("OldPrecision");
		OldMandatory = "true".equals(map.getString("OldMandatory"));
	}

	@Override
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Table", Table);
		map.put("Column", Column);
		map.put("DataType", DataType);
		map.put("Length", Length);
		map.put("Precision", Precision);
		map.put("Mandatory", Mandatory);
		map.put("OldDataType", OldDataType);
		map.put("OldLength", OldLength);
		map.put("OldPrecision", OldPrecision);
		map.put("OldMandatory", OldMandatory);
		return Prefix + JSON.toJSONString(map);
	}
}
