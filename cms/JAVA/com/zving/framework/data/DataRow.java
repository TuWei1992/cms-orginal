package com.zving.framework.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.FrameworkException;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 数据行，表示DataTable中的一行数据
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2005-7-15
 */
public class DataRow implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	/**
	 * 所在DataTable
	 */
	protected DataTable table;

	/**
	 * 各列的值
	 */
	protected final ArrayList<Object> values;

	/**
	 * 构造器
	 * 
	 * @param table 所在的DataTable
	 * @param values 各列的值
	 */
	public DataRow(DataTable table, ArrayList<Object> values) {
		this.table = table;
		this.values = values;
	}

	/**
	 * 构造器
	 * 
	 * @param table 所在的DataTable
	 * @param valueArr 各列的值
	 */
	public DataRow(DataTable table, Object[] valueArr) {
		this.table = table;
		values = new ArrayList<Object>(table.columns.length);
		if (valueArr == null) {
			valueArr = new Object[table.columns.length];
		}
		for (int i = 0; i < valueArr.length && i < table.columns.length; i++) {
			values.add(valueArr[i]);
		}
		if (valueArr.length < table.columns.length) {
			for (int i = valueArr.length; i < table.columns.length; i++) {
				values.add(null);
			}
		}
	}

	public Object get(int index) {
		if (values == null || index == -1) {
			return null;
		}
		return values.get(index);
	}

	/**
	 * 获取指定字段名对应的值
	 * 
	 * @param columnName 字段名
	 * @return 字段的值
	 */
	public Object get(String columnName) {
		int i = table.getColumnIndex(columnName);
		if (i < 0 && table.getColumnCount() > 0) {
			return null;
		}
		return get(i);
	}

	/**
	 * 获取指定顺序的字段值并转换成字符串，如果是日期类型并且相应的字段有指定日期格式，则会按日期格式格式化成字符串。<br>
	 * 注意：本方法不能返回null，如果值不存在或者为null则必须返回空串。很多项目代码未对本方法的返回结果做检查
	 * 
	 * @param index 字段顺序
	 * @return 字段的值
	 */
	public String getString(int index) {
		if (index == -1) {
			return "";
		}
		Object v = get(index);
		if (v != null) {
			if (!"".equals(v) && table.columns[index].getColumnType() == DataTypes.DATETIME) {
				if (!(v instanceof Date)) {// DataTable.set或DataRow.set时不会校验类型，所以值的类型可能不再是Date了
					return String.valueOf(v);
				}
				if (StringUtil.isNotEmpty(table.columns[index].getDateFormat())) {
					return DateUtil.toString((Date) v, table.columns[index].getDateFormat());
				} else {
					return DateUtil.toDateTimeString((Date) v);
				}
			}
			String t = String.valueOf(v).trim();
			if (table.isWebMode()) {
				if (t == null || t.equals("")) {
					return "&nbsp;";
				}
			}
			return t;
		} else {
			if (table.isWebMode()) {
				return "&nbsp;";
			}
			return "";
		}
	}

	/**
	 * 获取指定字段名的值并转换成字符串，如果是日期类型并且相应的字段有指定日期格式，则会按日期格式格式化成字符串。
	 * 
	 * @param columnName 字段名
	 * @return 字段值转换后的字符串
	 */
	public String getString(String columnName) {
		int i = table.getColumnIndex(columnName);
		if (i < 0 && table.getColumnCount() > 0) {
			return null;
		}
		return getString(i);
	}

	/**
	 * 按顺序获取字段值对应的Date实例
	 * 
	 * @param index 字段顺序
	 * @return 字段值转换成的日期
	 */
	public Date getDate(int index) {
		Object obj = get(index);
		if (obj == null) {
			return null;
		}
		if (obj instanceof Date) {
			return (Date) obj;
		} else {
			return DateUtil.parseDateTime(obj.toString());
		}
	}

	/**
	 * 按字段名称获取字段值对应的Date实例
	 * 
	 * @param columnName 字段名称
	 * @return 字段值转换成的日期
	 */
	public Date getDate(String columnName) {
		int i = table.getColumnIndex(columnName);
		if (i < 0 && table.getColumnCount() > 0) {
			return null;
		}
		return getDate(i);
	}

	/**
	 * 按顺序获取字段值对应的双字节浮点型，如果字段值为空，则返回0
	 * 
	 * @param index 字段顺序
	 * @return 字段值转换成的双字节浮点型
	 */
	public double getDouble(int index) {
		Object obj = get(index);
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		} else {
			String str = obj.toString();
			if (StringUtil.isEmpty(str)) {
				return 0;
			}
			return Double.parseDouble(str);
		}
	}

	/**
	 * 按字段名称获取字段值对应的双字节浮点型，如果字段值为空，则返回0
	 * 
	 * @param columnName 字段名称
	 * @return 字段值转换成的双字节浮点型
	 */
	public double getDouble(String columnName) {
		int i = table.getColumnIndex(columnName);
		if (i < 0 && table.getColumnCount() > 0) {
			return 0;
		}
		return getDouble(i);
	}

	/**
	 * 按顺序获取字段值对应的浮点型，如果字段值为空，则返回0
	 * 
	 * @param index 字段顺序
	 * @return 字段值转换成的浮点型
	 */
	public float getFloat(int index) {
		Object obj = get(index);
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).floatValue();
		} else {
			String str = obj.toString();
			if (StringUtil.isEmpty(str)) {
				return 0;
			}
			return Float.parseFloat(str);
		}
	}

	/**
	 * 按字段名称获取字段值对应的浮点型，如果字段值为空，则返回0
	 * 
	 * @param columnName 字段名称
	 * @return 字段值转换成的浮点型
	 */
	public float getFloat(String columnName) {
		int i = table.getColumnIndex(columnName);
		if (i < 0 && table.getColumnCount() > 0) {
			return 0;
		}
		return getFloat(i);
	}

	/**
	 * 按顺序获取字段值对应的长整型，如果字段值为空，则返回0
	 * 
	 * @param index 字段顺序
	 * @return 字段值转换成的长整型
	 */
	public long getLong(int index) {
		Object obj = get(index);
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		} else {
			String str = obj.toString();
			if (StringUtil.isEmpty(str)) {
				return 0;
			}
			return Long.parseLong(str);
		}
	}

	/**
	 * 按字段名称获取字段值对应的长整型，如果字段值为空，则返回0
	 * 
	 * @param columnName 字段名称
	 * @return 字段值转换成的长整型
	 */
	public long getLong(String columnName) {
		int i = table.getColumnIndex(columnName);
		if (i < 0 && table.getColumnCount() > 0) {
			return 0;
		}
		return getLong(i);
	}

	/**
	 * 按顺序获取字段值对应的整型，如果字段值为空，则返回0
	 * 
	 * @param index 字段顺序
	 * @return 字段值转换成的整型
	 */
	public int getInt(int index) {
		Object obj = get(index);
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).intValue();
		} else {
			String str = obj.toString();
			if (StringUtil.isEmpty(str)) {
				return 0;
			}
			return Integer.parseInt(str);
		}
	}

	/**
	 * 按字段名称获取字段值对应的整型，如果字段值为空，则返回0
	 * 
	 * @param columnName 字段名称
	 * @return 字段值转换成的整型
	 */
	public int getInt(String columnName) {
		int i = table.getColumnIndex(columnName);
		if (i < 0 && table.getColumnCount() > 0) {
			return 0;
		}
		return getInt(i);
	}

	/**
	 * 按顺序设置字段的值
	 * 
	 * @param index 字段顺序
	 * @param value 值
	 * @return 实例本身
	 */
	public DataRow set(int index, Object value) {
		if (values == null) {
			return this;
		}
		values.set(index, value);
		return this;
	}

	/**
	 * 按字段名称设置字段的值
	 * 
	 * @param columnName 字段名称
	 * @param value 字段值
	 * @return 实例本身
	 */
	public DataRow set(String columnName, Object value) {
		int i = table.getColumnIndex(columnName);
		if (i < 0 && table.getColumnCount() > 0) {
			return this;
		}
		return set(i, value);
	}

	/**
	 * 按顺序获取DataColumn实例
	 * 
	 * @param index 字段顺序
	 * @return 指定顺序的DataColumn实例
	 */
	public DataColumn getDataColumn(int index) {// NO_UCD
		return table.getDataColumn(index);
	}

	/**
	 * 按字段名称获取DataColumn实例
	 * 
	 * @param columnName 字段名称
	 * @return 字段名称对应的DataColumn实例
	 */
	public DataColumn getDataColumn(String columnName) {
		return table.getDataColumn(columnName);
	}

	/**
	 * @return 字段数量
	 */
	public int getColumnCount() {
		return table.getColumnCount();
	}

	/**
	 * @return 所有字段的值
	 */
	public Object[] getDataValues() {
		return values.toArray();
	}

	/**
	 * @return 所有字段
	 */
	public DataColumn[] getDataColumns() {
		return table.getDataColumns();
	}

	/**
	 * @return 转换为Mapx
	 */
	public Mapx<String, Object> toMapx() {
		Mapx<String, Object> map = new Mapx<String, Object>();
		for (int i = 0; i < table.columns.length; i++) {
			map.put(table.columns[i].getColumnName(), values.get(i));
		}
		return map;
	}

	/**
	 * @return 转换为键值忽略大小写的Mapx
	 */
	public CaseIgnoreMapx<String, Object> toCaseIgnoreMapx() {
		CaseIgnoreMapx<String, Object> map = new CaseIgnoreMapx<String, Object>();
		for (int i = 0; i < table.columns.length; i++) {
			map.put(table.columns[i].getColumnName(), values.get(i));
		}
		return map;
	}

	/**
	 * 将Map中的键对应的值设置到与键同名的字段上，忽略键和字段名的大小写差异。
	 * 
	 * @param map Map
	 */
	public void fill(Map<?, ?> map) {// Map中的键和列名可能有大小写差异
		if (map == null) {
			return;
		}
		for (Object key : map.keySet()) {
			if (key == null) {
				continue;
			}
			Object v = map.get(key);
			for (int j = 0; j < table.columns.length; j++) {
				if (key.toString().equalsIgnoreCase(table.columns[j].getColumnName())) {
					if (v != null && table.columns[j].getColumnType() == DataTypes.DATETIME) {
						if (!Date.class.isInstance(v)) {
							Date d = DateUtil.parseDateTime(v.toString());
							if (d == null) {
								throw new FrameworkException("Invalid date string:" + v);
							}
							v = d;
						}
					}
					set(j, v);
				}
			}

		}
	}

	/**
	 * 将值数组中的数据按顺序设置到相应的字段上
	 * 
	 * @param values 值列表
	 */
	public void fill(Object... values) {// NO_UCD
		if (values == null) {
			return;
		}
		if (values.length != getColumnCount()) {
			throw new FrameworkException("Parameter's length is " + values.length + "，bit DataRow's length is " + getColumnCount());
		}
		for (int i = 0; i < values.length; i++) {
			Object v = values[i];
			if (v != null && table.columns[i].getColumnType() == DataTypes.DATETIME) {
				if (!Date.class.isInstance(v)) {
					Date d = DateUtil.parseDateTime(v.toString());
					if (d == null) {
						throw new FrameworkException("Invalid date string:" + v);
					}
					v = d;
				}
			}
			set(i, v);
		}
		return;
	}

	/**
	 * 克隆
	 */
	@Override
	public DataRow clone() {
		@SuppressWarnings("unchecked")
		ArrayList<Object> vs = (ArrayList<Object>) values.clone();
		return new DataRow(table, vs);
	}

	/**
	 * 输出成字符串
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < table.columns.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(table.columns[i].getColumnName());
			sb.append(":");
			sb.append(values.get(i));
		}
		return sb.toString();
	}

	/**
	 * @return 所在DataTable
	 */
	public DataTable getTable() {
		return table;
	}
}
