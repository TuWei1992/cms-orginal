package com.zving.framework.json.convert;

import java.util.Date;

import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.json.JSONArray;
import com.zving.framework.json.JSONObject;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.StringUtil;

/**
 * DataTable的JSON转换器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-17
 */
public class DataTableConvertor implements IJSONConvertor {

	@Override
	public String getExtendItemID() {
		return "DataTable";
	}

	@Override
	public String getExtendItemName() {
		return getExtendItemID();
	}

	@Override
	public boolean match(Object obj) {
		return obj instanceof DataTable;
	}

	@Override
	public JSONObject toJSON(Object obj) {
		JSONObject jo = new JSONObject();
		DataTable dt = (DataTable) obj;
		jo.put("Columns", dt.getDataColumns());
		Object[][] vs = new Object[dt.getRowCount()][dt.getColumnCount()];
		for (int j = 0; j < dt.getRowCount(); j++) {
			vs[j] = dt.getDataRow(j).getDataValues();
		}
		for (int i = 0; i < dt.getColumnCount(); i++) {
			DataColumn dc = dt.getDataColumn(i);
			if (dc.getColumnType() == DataTypes.DATETIME) {// 日期字段应该自动格式化
				for (int j = 0; j < dt.getRowCount(); j++) {
					Object v = vs[j][i];
					if (v instanceof Date) {
						if (StringUtil.isNotEmpty(dc.getDateFormat())) {
							v = DateUtil.toString((Date) v, dc.getDateFormat());
						} else {
							v = DateUtil.toDateTimeString((Date) v);
						}
					}
					vs[j][i] = v;
				}
			}
		}

		jo.put("Values", vs);
		jo.put("@type", "DataTable");
		return jo;
	}

	@Override
	public Object fromJSON(JSONObject map) {
		JSONArray columns = map.getJSONArray("Columns");
		JSONArray values = map.getJSONArray("Values");
		DataColumn[] dcs = new DataColumn[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			dcs[i] = new DataColumn();
			JSONObject col = columns.getJSONObject(i);
			dcs[i].setColumnName(col.getString("Name"));
			dcs[i].setColumnType(col.getInt("Type"));
		}

		Object[][] vs = null;
		if (values.size() > 0) {
			vs = new Object[values.size()][dcs.length];
			for (int i = 0; i < values.size(); i++) {
				vs[i] = values.getJSONArray(i).toArray();
			}
		}
		return new DataTable(dcs, vs);
	}

}
