package com.zving.framework.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.RandomAccess;

import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Filter;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.FrameworkException;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.thirdparty.commons.ArrayUtils;
import com.zving.framework.utility.StringUtil;

/**
 * 数据表格，主要用来封装ResultSet。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2005-7-6
 */
public class DataTable implements Serializable, Cloneable, RandomAccess, Iterable<DataRow> {
	private static final long serialVersionUID = 1L;

	private boolean isWebMode;// 默认值为false，True表示getString的结果是null或者""时转成&nbsp;

	private ArrayList<DataRow> rows = new ArrayList<DataRow>();// 数据表中的所有行

	DataColumn[] columns;// 所有字段

	/**
	 * 构造器
	 */
	public DataTable() {
		columns = new DataColumn[0];
	}

	/**
	 * 构造器
	 * 
	 * @param columns 字段列表
	 * @param values 各行的值
	 */
	public DataTable(DataColumn[] columns, Object[][] values) {
		if (columns == null) {
			columns = new DataColumn[0];
		}
		this.columns = columns;
		renameAmbiguousColumns();// 将名称相同的列重命名
		if (values != null) {
			for (Object[] value : values) {
				rows.add(new DataRow(this, value));
			}
		}
	}

	/**
	 * 构造器
	 * 
	 * @param rs ResultSet
	 */
	public DataTable(DBConn conn, ResultSet rs) {
		this(conn, rs, Integer.MAX_VALUE, 0, false);
	}

	/**
	 * 构造器
	 * 
	 * @param rs ResultSet
	 * @param latin1Flag 是否为latin1字符集，在oracle下使用此字符集需要特殊处理
	 */
	public DataTable(DBConn conn, ResultSet rs, boolean latin1Flag) {
		this(conn, rs, Integer.MAX_VALUE, 0, latin1Flag);
	}

	/**
	 * 构造器
	 * 
	 * @param rs ResultSet
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页，0为第一页
	 */
	public DataTable(DBConn conn, ResultSet rs, int pageSize, int pageIndex) {// NO_UCD
		this(conn, rs, pageSize, pageIndex, false);
	}

	/**
	 * 构造器
	 * 
	 * @param rs ResultSet
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页，0为第一页
	 * @param latin1Flag 是否为latin1字符集，在oracle下使用此字符集需要特殊处理
	 */
	public DataTable(DBConn conn, ResultSet rs, int pageSize, int pageIndex, boolean latin1Flag) {
		ResultSetMetaData rsmd;
		try {
			// 以下准备DataColumn[]
			rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			DataColumn[] types = new DataColumn[columnCount];
			for (int i = 1; i <= columnCount; i++) {
				String name = rsmd.getColumnLabel(i);
				boolean b = rsmd.isNullable(i) == ResultSetMetaData.columnNullable;
				DataColumn dc = new DataColumn();
				dc.setAllowNull(b);
				dc.setColumnName(name);

				// 以下设置数据类型
				int dataType = rsmd.getColumnType(i);
				if (dataType == Types.CHAR || dataType == Types.VARCHAR) {
					dc.setColumnType(DataTypes.STRING);
				} else if (dataType == Types.TIMESTAMP || dataType == Types.DATE) {
					dc.setColumnType(DataTypes.DATETIME);
				} else if (dataType == Types.DECIMAL) {
					dc.setColumnType(DataTypes.DECIMAL);
					int dataScale = rsmd.getScale(i);
					int dataPrecision = rsmd.getPrecision(i);
					if (dataScale == 0 && dataPrecision != 0) {
						dc.setColumnType(DataTypes.LONG);
					} else if (dataScale > 0 && dataScale + dataPrecision > 17) {// 双精度有效十进制位数为17
						dc.setColumnType(DataTypes.BIGDECIMAL);
					} else {
						dc.setColumnType(DataTypes.DECIMAL);
					}
				} else if (dataType == Types.DOUBLE || dataType == Types.REAL) {
					dc.setColumnType(DataTypes.DOUBLE);
				} else if (dataType == Types.FLOAT) {
					dc.setColumnType(DataTypes.FLOAT);
				} else if (dataType == Types.INTEGER) {
					dc.setColumnType(DataTypes.INTEGER);
				} else if (dataType == Types.SMALLINT || dataType == Types.TINYINT) {
					dc.setColumnType(DataTypes.SMALLINT);
				} else if (dataType == Types.BIT) {
					dc.setColumnType(DataTypes.BIT);
				} else if (dataType == Types.BIGINT) {
					dc.setColumnType(DataTypes.LONG);
				} else if (dataType == Types.BLOB || dataType == Types.LONGVARBINARY) {
					dc.setColumnType(DataTypes.BLOB);
				} else if (dataType == Types.CLOB || dataType == Types.LONGVARCHAR) {
					dc.setColumnType(DataTypes.CLOB);
				} else if (dataType == Types.NUMERIC) {
					int dataScale = rsmd.getScale(i);
					int dataPrecision = rsmd.getPrecision(i);
					if (dataScale == 0 && dataPrecision != 0) {
						dc.setColumnType(DataTypes.LONG);
					} else if (dataScale > 0 && dataScale + dataPrecision > 17) {// 双精度有效十进制位数为17
						dc.setColumnType(DataTypes.BIGDECIMAL);
					} else {
						dc.setColumnType(DataTypes.DOUBLE);
					}
				} else {
					dc.setColumnType(DataTypes.STRING);
				}
				types[i - 1] = dc;
			}

			columns = types;
			renameAmbiguousColumns();

			IDBType db = DBTypeService.getInstance().get(conn.getDBConfig().DBType);

			// 以下准备ColumnValues[]
			int index = 0;
			int begin = pageIndex * pageSize;
			int end = (pageIndex + 1) * pageSize;
			while (rs.next()) {
				if (index >= end) {
					break;
				}
				if (index >= begin) {
					Object[] rowValue = new Object[columnCount];
					for (int j = 1; j <= columnCount; j++) {
						int columnType = columns[j - 1].getColumnType();
						rowValue[j - 1] = db.getValueFromResultSet(rs, j, columnType, latin1Flag);
					}
					rows.add(new DataRow(this, rowValue));
				}
				index++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将同名的字段重命名，规则是在原字段名后加“_1”之类的后缀
	 */
	private void renameAmbiguousColumns() {
		if (columns == null) {
			return;
		}
		for (int i = 0; i < columns.length; i++) {
			String columnName = columns[i].getColumnName();
			int count = 1;
			for (int j = i + 1; j < columns.length; j++) {
				if (columnName == null) {
					throw new FrameworkException("Column name cann't be null,index is " + i);
				}
				if (columnName.equalsIgnoreCase(columns[j].getColumnName())) {
					columns[j].setColumnName(columnName + "_" + String.valueOf(++count));
				}
			}
		}
	}

	/**
	 * 删除指定顺序的字段
	 * 
	 * @param columnIndex 字段顺序
	 * @return DataTable本身
	 */
	public DataTable deleteColumn(int columnIndex) {
		if (columns.length == 0) {
			return this;
		}
		if (columnIndex < 0 || columnIndex >= columns.length) {
			throw new FrameworkException("Index is out of range：" + columnIndex);
		}
		columns = ArrayUtils.remove(columns, columnIndex);
		for (int i = 0; i < rows.size(); i++) {
			rows.get(i).values.remove(columnIndex);
		}
		return this;
	}

	/**
	 * 删除字段名称对应的字段
	 * 
	 * @param columnName 字段名称
	 * @return DataTable本身
	 */
	public DataTable deleteColumn(String columnName) {
		int i = getColumnIndex(columnName);
		if (i >= 0) {
			deleteColumn(i);
		}
		return this;
	}

	/**
	 * 插入一个字段，字段类型为DataTypes.STRING
	 * 
	 * @param columnName 字段名称
	 * @return DataTable本身
	 */
	public DataTable insertColumn(String columnName) {
		return insertColumn(new DataColumn(columnName, DataTypes.STRING), null, columns.length);
	}

	/**
	 * 一次插入多个字段，字段类型都为DataTypes.STRING
	 * 
	 * @param columnNames 字段名称数组
	 * @return DataTable本身
	 */
	public DataTable insertColumns(String... columnNames) {
		for (String columnName : columnNames) {
			insertColumn(new DataColumn(columnName, DataTypes.STRING), null, columns.length);
		}
		return this;
	}

	/**
	 * 插入一个字段，字段类型为DataTypes.STRING，并将此字段在所有行中的值置为columnValue
	 * 
	 * @param columnName 字段名称
	 * @param columnValue 字段值
	 * @return DataTable本身
	 */
	public DataTable insertColumn(String columnName, Object columnValue) {
		Object[] cv = new Object[rows.size()];
		for (int i = 0; i < cv.length; i++) {
			cv[i] = columnValue;
		}
		return insertColumn(new DataColumn(columnName, DataTypes.STRING), cv, columns.length);
	}

	/**
	 * 插入一个字段，字段类型为DataTypes.STRING，此字段各行的值由values数组指定
	 * 
	 * @param columnName 字段名称
	 * @param values 值数组
	 * @return DataTable本身
	 */
	public DataTable insertColumn(String columnName, Object[] values) {
		return insertColumn(new DataColumn(columnName, DataTypes.STRING), values, columns.length);
	}

	/**
	 * 插入一个字段
	 * 
	 * @param dc 字段信息
	 * @return DataTable本身
	 */
	public DataTable insertColumn(DataColumn dc) {
		return insertColumn(dc, null, columns.length);
	}

	/**
	 * 插入一个字段，此字段在各行的值由values数组指定
	 * 
	 * @param dc 字段信息
	 * @param values 各行的值
	 * @return DataTable本身
	 */
	public DataTable insertColumn(DataColumn dc, Object[] values) {// NO_UCD
		return insertColumn(dc, values, columns.length);
	}

	/**
	 * 在指定位置上插入一个字段，字段类型为DataTypes.STRING,此字段在各行的值由values数组指定
	 * 
	 * @param columnName 字段名称
	 * @param values 各行的值
	 * @param index 字段位置
	 * @return DataTable本身
	 */
	public DataTable insertColumn(String columnName, Object[] values, int index) {// NO_UCD
		return insertColumn(new DataColumn(columnName, DataTypes.STRING), values, index);
	}

	/**
	 * 在指定位置上插入一个字段，此字段在各行的值由values数组指定
	 * 
	 * @param dc 字段信息
	 * @param values 各行的值
	 * @param index 字段位置
	 * @return DataTable本身
	 */
	public DataTable insertColumn(DataColumn dc, Object[] values, int index) {
		if (index > columns.length) {
			throw new FrameworkException("Index is out of range:" + index);
		}
		if (getDataColumn(dc.getColumnName()) != null) {
			return this;
		}
		columns = ArrayUtils.add(columns, index, dc);
		if (values == null) {
			values = new Object[rows.size()];
		}
		for (int i = 0; i < rows.size() && i < values.length; i++) {
			rows.get(i).values.add(index, values[i]);
		}
		return this;
	}

	/**
	 * 插入一个数据行
	 * 
	 * @param dr 数据行
	 * @return DataTable本身
	 */
	public DataTable insertRow(DataRow dr) {
		return insertRow(dr, rows.size());
	}

	/**
	 * 在指定位置上插入一个数据行
	 * 
	 * @param dr 数据行
	 * @param index 行顺序
	 * @return DataTable本身
	 */
	public DataTable insertRow(DataRow dr, int index) {
		if (columns == null || columns.length == 0) {
			columns = dr.table.columns;
		}
		dr = dr.clone();
		dr.table = this;
		rows.add(index, dr);
		return this;
	}

	/**
	 * 插入一个数据行，此数据行的各个字段的值由columnValues指定
	 * 
	 * @param columnValues 各个字段的值
	 * @return DataTable本身
	 */
	public DataTable insertRow(Object... columnValues) {
		return insertRow(columnValues, rows.size());
	}

	/**
	 * 在指定位置上插入一个数据行，此数据行的各个字段的值由columnValues指定
	 * 
	 * @param columnValues 各个字段的值
	 * @param index 行顺序
	 * @return DataTable本身
	 */
	public DataTable insertRow(Object[] columnValues, int index) {
		DataRow dr = new DataRow(this, columnValues);
		rows.add(index, dr);
		return this;
	}

	/**
	 * 删除指定位置的数据行
	 * 
	 * @param index 行顺序
	 * @return DataTable本身
	 */
	public DataTable deleteRow(int index) {
		rows.remove(index);
		return this;
	}

	/**
	 * 删除指定的数据行
	 * 
	 * @param dr 数据行
	 * @return DataTable本身
	 */
	public DataTable deleteRow(DataRow dr) {
		rows.remove(dr);
		return this;
	}

	/**
	 * 获得指定顺序的数据行
	 * 
	 * @param rowIndex 行顺序
	 * @return 数据行
	 */
	public DataRow get(int rowIndex) {
		return getDataRow(rowIndex);
	}

	/**
	 * 设置指定数据行上的指定顺序的字段的值
	 * 
	 * @param rowIndex 行顺序
	 * @param columnIndex 字段顺序
	 * @param value 字段值
	 * @return DataTable本身
	 */
	public DataTable set(int rowIndex, int columnIndex, Object value) {
		getDataRow(rowIndex).set(columnIndex, value);
		return this;
	}

	/**
	 * 设置指定数据行上的指定字段名的字段的值
	 * 
	 * @param rowIndex 行顺序
	 * @param columnName 字段名称
	 * @param value 字段值
	 * @return DataTable本身
	 */
	public DataTable set(int rowIndex, String columnName, Object value) {
		getDataRow(rowIndex).set(columnName, value);
		return this;
	}

	/**
	 * 获取指定顺序的行上的指定顺序的字段的值
	 * 
	 * @param rowIndex 行顺序
	 * @param columnIndex 字段顺序
	 * @return 字段值
	 */
	public Object get(int rowIndex, int columnIndex) {
		return getDataRow(rowIndex).get(columnIndex);
	}

	/**
	 * 获取指定顺序的行上的指定字段的值
	 * 
	 * @param rowIndex 行顺序
	 * @param columnName 字段名称
	 * @return 字段值
	 */
	public Object get(int rowIndex, String columnName) {
		return getDataRow(rowIndex).get(columnName);
	}

	/**
	 * 获取指定顺序的行上的指定顺序的字段的值，并转化为String。
	 * 如果isWebMode()为true，则会将null值和空字符串转为&amp;nbsp;返回
	 * 
	 * @param rowIndex 行顺序
	 * @param columnIndex 字段顺序
	 * @return 字段值转换成的String实例
	 */
	public String getString(int rowIndex, int columnIndex) {
		return getDataRow(rowIndex).getString(columnIndex);
	}

	/**
	 * 获取指定顺序的行上的指定字段的值，并转化为String。
	 * 如果isWebMode()为true，则会将null值和空字符串转为&amp;nbsp;返回
	 * 
	 * @param rowIndex 行顺序
	 * @param columnName 字段名称
	 * @return 字段值转换成的String实例
	 */
	public String getString(int rowIndex, String columnName) {
		return getDataRow(rowIndex).getString(columnName);
	}

	/**
	 * 获取指定顺序的行上的指定顺序的字段的值，并转化为整型。
	 * 如果字段值为null，则返回0
	 * 
	 * @param rowIndex 行顺序
	 * @param columnIndex 字段顺序
	 * @return 字段值转换成的整型
	 */
	public int getInt(int rowIndex, int columnIndex) {
		return getDataRow(rowIndex).getInt(columnIndex);
	}

	/**
	 * 获取指定顺序的行上的指定字段的值，并转化为整型。
	 * 如果字段值为null，则返回0
	 * 
	 * @param rowIndex 行顺序
	 * @param columnName 字段名称
	 * @return 字段值转换成的整型
	 */
	public int getInt(int rowIndex, String columnName) {
		return getDataRow(rowIndex).getInt(columnName);
	}

	/**
	 * 获取指定顺序的行上的指定顺序的字段的值，并转化为长整型。
	 * 如果字段值为null，则返回0
	 * 
	 * @param rowIndex 行顺序
	 * @param columnIndex 字段顺序
	 * @return 字段值转换成的长整型
	 */
	public long getLong(int rowIndex, int columnIndex) {
		return getDataRow(rowIndex).getLong(columnIndex);
	}

	/**
	 * 获取指定顺序的行上的指定字段的值，并转化为长整型。
	 * 如果字段值为null，则返回0
	 * 
	 * @param rowIndex 行顺序
	 * @param columnName 字段名称
	 * @return 字段值转换成的长整型
	 */
	public long getLong(int rowIndex, String columnName) {
		return getDataRow(rowIndex).getLong(columnName);
	}

	/**
	 * 获取指定顺序的行上的指定顺序的字段的值，并转化为双字节浮点型。
	 * 如果字段值为null，则返回0
	 * 
	 * @param rowIndex 行顺序
	 * @param columnIndex 字段顺序
	 * @return 字段值转换成的双字节浮点型
	 */
	public double getDouble(int rowIndex, int columnIndex) {// NO_UCD
		return getDataRow(rowIndex).getDouble(columnIndex);
	}

	/**
	 * 获取指定顺序的行上的指定字段的值，并转化为双字节浮点型。
	 * 如果字段值为null，则返回0
	 * 
	 * @param rowIndex 行顺序
	 * @param columnName 字段名称
	 * @return 字段值转换成的双字节浮点型
	 */
	public double getDouble(int rowIndex, String columnName) {// NO_UCD
		return getDataRow(rowIndex).getDouble(columnName);
	}

	/**
	 * 获取指定顺序的行上的指定顺序的字段的值，并转化为浮点型。
	 * 如果字段值为null，则返回0
	 * 
	 * @param rowIndex 行顺序
	 * @param columnIndex 字段顺序
	 * @return 字段值转换成的浮点型
	 */
	public float getFloat(int rowIndex, int columnIndex) {// NO_UCD
		return getDataRow(rowIndex).getFloat(columnIndex);
	}

	/**
	 * 获取指定顺序的行上的指定字段的值，并转化为浮点型。
	 * 如果字段值为null，则返回0
	 * 
	 * @param rowIndex 行顺序
	 * @param columnName 字段名称
	 * @return 字段值转换成的浮点型
	 */
	public float getFloat(int rowIndex, String columnName) {// NO_UCD
		return getDataRow(rowIndex).getFloat(columnName);
	}

	/**
	 * 获取指定顺序的行上的指定顺序的字段的值，并转化为日期类型。
	 * 
	 * @param rowIndex 行顺序
	 * @param columnIndex 字段顺序
	 * @return 字段值转换成的日期类型
	 */
	public Date getDate(int rowIndex, int columnIndex) {// NO_UCD
		return getDataRow(rowIndex).getDate(columnIndex);
	}

	/**
	 * 获取指定顺序的行上的指定字段的值，并转化为日期类型。
	 * 
	 * @param rowIndex 行顺序
	 * @param columnName 字段名称
	 * @return 字段值转换成的日期类型
	 */
	public Date getDate(int rowIndex, String columnName) {
		return getDataRow(rowIndex).getDate(columnName);
	}

	/**
	 * @param rowIndex 行顺序
	 * @return 指定行顺序上的数据行实例
	 */
	public DataRow getDataRow(int rowIndex) {
		if (rowIndex >= rows.size() || rowIndex < 0) {
			throw new FrameworkException("Index is out of range:" + rowIndex);
		}
		return rows.get(rowIndex);
	}

	/**
	 * @param columnIndex 字段顺序
	 * @return 指定顺序的字段信息实例
	 */
	public DataColumn getDataColumn(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= columns.length) {
			throw new FrameworkException("Index is out of range:" + columnIndex);
		}
		return columns[columnIndex];
	}

	/**
	 * @param columnName 字段名称
	 * @return 指定字段的字段信息实例
	 */
	public DataColumn getDataColumn(String columnName) {
		int i = getColumnIndex(columnName);
		if (i == -1) {
			return null;
		}
		return columns[i];
	}

	/**
	 * 获取指定顺序的字段在所有行上的值
	 * 
	 * @param columnIndex 字段顺序
	 * @return 各行的值组成的数组
	 */
	public Object[] getColumnValues(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= columns.length) {
			throw new FrameworkException("Index is out of range:" + columnIndex);
		}
		Object[] arr = new Object[getRowCount()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = rows.get(i).values.get(columnIndex);
		}
		return arr;
	}

	/**
	 * 获取指定字段在所有行上的值
	 * 
	 * @param columnName 字段名称
	 * @return 各行的值组成的数组
	 */
	public Object[] getColumnValues(String columnName) {
		int i = getColumnIndex(columnName);
		if (i < 0) {
			return null;
		}
		return getColumnValues(i);
	}

	/**
	 * 根据比较器对数据行进行排序
	 * 
	 * @param c 比较器
	 */
	public void sort(Comparator<DataRow> c) {
		Collections.sort(rows, c);
	}

	/**
	 * 将DataTable中的数据行按指定字段进行逆序排列
	 * 
	 * @param columnName 字段名称
	 */
	public void sort(String columnName) {
		sort(columnName, "desc", false);
	}

	/**
	 * 将DataTable中的数据行按指定字段和排序方向进行排序
	 * 
	 * @param columnName 字段名称
	 * @param order 排序方向，ASC顺序，DESC逆序
	 */
	public void sort(String columnName, String order) {
		sort(columnName, order, false);
	}

	/**
	 * 将DataTable中的数据行按指定字段和排序方向进行排序
	 * 
	 * @param columnName 字段名称
	 * @param order 排序方向，ASC顺序，DESC逆序
	 * @param isNumber 将字段的值作为数字进行排序
	 */
	public void sort(String columnName, String order, final boolean isNumber) {
		final String cn = columnName;
		final String od = order;
		sort(new Comparator<DataRow>() {
			@Override
			public int compare(DataRow dr1, DataRow dr2) {
				Object v1 = dr1.get(cn);
				Object v2 = dr2.get(cn);
				if (v1 instanceof Number && v2 instanceof Number) {
					double d1 = ((Number) v1).doubleValue();
					double d2 = ((Number) v2).doubleValue();
					if (d1 == d2) {
						return 0;
					} else if (d1 > d2) {
						return "asc".equalsIgnoreCase(od) ? 1 : -1;
					} else {
						return "asc".equalsIgnoreCase(od) ? -1 : 1;
					}
				} else if (v1 instanceof Date && v2 instanceof Date) {
					Date d1 = (Date) v1;
					Date d2 = (Date) v2;
					if ("asc".equalsIgnoreCase(od)) {
						return d1.compareTo(d2);
					} else {
						return -d1.compareTo(d2);
					}
				} else if (isNumber) {
					double d1 = 0, d2 = 0;
					try {
						d1 = Double.parseDouble(String.valueOf(v1));
						d2 = Double.parseDouble(String.valueOf(v2));
					} catch (Exception e) {
					}
					if (d1 == d2) {
						return 0;
					} else if (d1 > d2) {
						return "asc".equalsIgnoreCase(od) ? -1 : 1;
					} else {
						return "asc".equalsIgnoreCase(od) ? 1 : -1;
					}
				} else {
					int c = dr1.getString(cn).compareTo(dr2.getString(cn));
					if ("asc".equalsIgnoreCase(od)) {
						return c;
					} else {
						return -c;
					}
				}
			}
		});
	}

	/**
	 * 过滤掉部分记录后生成一个新的DataTable
	 * 
	 * @param filter 过滤器
	 * @return 过滤后的新的DataTable实例
	 */
	public DataTable filter(Filter<DataRow> filter) {
		DataTable dt = new DataTable(columns, null);
		dt.setWebMode(isWebMode);
		for (DataRow row : rows) {
			if (filter.filter(row)) {
				dt.insertRow(row.clone());
			}
		}
		return dt;
	}

	/**
	 * 克隆DataTable
	 */
	@Override
	public DataTable clone() {
		DataColumn[] dcs = new DataColumn[columns.length];
		for (int i = 0; i < columns.length; i++) {
			dcs[i] = (DataColumn) columns[i].clone();
		}
		DataTable dt = new DataTable(dcs, null);
		for (DataRow dr : rows) {
			@SuppressWarnings("unchecked")
			ArrayList<Object> values = (ArrayList<Object>) dr.values.clone();
			dt.insertRow(new DataRow(dt, values));
		}
		dt.setWebMode(isWebMode);
		return dt;
	}

	/**
	 * 以指定名称对应的字段的值为key,以另一名称对应的字段的值为value,填充到一个Mapx中，并返回此Mapx
	 * 
	 * @param keyColumnName 作为键的字段的名称
	 * @param valueColumnName 作为值的字段的名称
	 * @return Mapx
	 */
	public Mapx<String, Object> toMapx(String keyColumnName, String valueColumnName) {
		int keyIndex = 0, valueIndex = 0;
		if ((keyIndex = getColumnIndex(keyColumnName)) == -1) {
			throw new FrameworkException("Key column name not found:" + keyColumnName);
		}
		if ((valueIndex = getColumnIndex(valueColumnName)) == -1) {
			throw new FrameworkException("Value column name not found:" + valueColumnName);
		}
		return toMapx(keyIndex, valueIndex);
	}

	/**
	 * 以指定顺序的字段的值为key,以另一指定顺序的字段的值为value,填充到一个Mapx中，并返回此Mapx
	 * 
	 * @param keyColumnIndex 作为键的字段的顺序
	 * @param valueColumnIndex 作为值的字段的顺序
	 * @return Mapx
	 */
	public Mapx<String, Object> toMapx(int keyColumnIndex, int valueColumnIndex) {
		if (keyColumnIndex < 0 || keyColumnIndex >= columns.length) {
			throw new FrameworkException("Key index is out of range:" + keyColumnIndex);
		}
		if (valueColumnIndex < 0 || valueColumnIndex >= columns.length) {
			throw new FrameworkException("Value index is out of range:" + valueColumnIndex);
		}
		Mapx<String, Object> map = new CaseIgnoreMapx<String, Object>();
		for (DataRow row : rows) {
			Object key = row.values.get(keyColumnIndex);
			if (key == null) {
				map.put(null, row.values.get(valueColumnIndex));
			} else {
				map.put(key.toString(), row.values.get(valueColumnIndex));
			}
		}
		return map;
	}

	/**
	 * 以字段名对应的字段的值为key，去map中寻找对应的值，并把值置到新增的列中，新增列的列名=指定列列名+"Name"
	 * 
	 * @param columnName 字段名称
	 * @param map Map
	 * @return DataTable本身
	 */
	public DataTable decodeColumn(String columnName, Map<?, ?> map) {
		return decodeColumn(getColumnIndex(columnName), map);
	}

	/**
	 * 以指定顺序的字段的值为key，去map中寻找对应的值，并把值置到新增的列中，新增列的列名=指定列列名+"Name"
	 * 
	 * @param columnIndex 字段顺序
	 * @param map Map
	 * @return DataTable本身
	 */
	public DataTable decodeColumn(int columnIndex, Map<?, ?> map) {
		if (columnIndex < 0 || columnIndex > columns.length) {
			return this;
		}
		String newName = columns[columnIndex].getColumnName() + "Name";
		int addIndex = columns.length;
		insertColumn(newName);
		for (int i = 0; i < getRowCount(); i++) {
			String v = getString(i, columnIndex);
			set(i, addIndex, map.get(v));
		}
		return this;
	}

	/**
	 * 清空数据
	 */
	public void clear(){
		rows = new ArrayList<DataRow>();
	}
	
	/**
	 * 将指定DataTable中的数据行合并到本实例中
	 * 
	 * @param anotherDT 待合并的DataTable
	 */
	public void union(DataTable anotherDT) {
		if (anotherDT.getRowCount() == 0) {
			return;
		}
		if (getColumnCount() != anotherDT.getColumnCount()) {
			throw new FrameworkException("This's column count is " + getColumnCount() + " ,but parameter's column column count is "
					+ anotherDT.getColumnCount());
		}
		rows.addAll(anotherDT.rows);
		for (DataRow dr : anotherDT.rows) {
			dr.table = this;
		}
	}

	/**
	 * 将DataTable分页
	 * 
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页，0为第一页
	 * @return 分页后的DataTable
	 */
	public DataTable getPagedDataTable(int pageSize, int pageIndex) {
		DataTable dt = new DataTable(columns, null);
		for (int i = pageIndex * pageSize; i < (pageIndex + 1) * pageSize && i < rows.size(); i++) {
			dt.insertRow(rows.get(i));
		}
		return dt;
	}

	/**
	 * @return 数据行数量
	 */
	public int getRowCount() {
		return rows.size();
	}

	/**
	 * 等同于getColumnCount()
	 * 
	 * @return 字段数量
	 */
	@Deprecated
	public int getColCount() {
		return columns.length;
	}

	/**
	 * @return 字段数量
	 */
	public int getColumnCount() {
		return columns.length;
	}

	/**
	 * @return 所有的字段信息
	 */
	public DataColumn[] getDataColumns() {
		return columns;
	}

	/**
	 * @return 是否是web模式。在web模式下getString()方法会将null或空字符串转化成&amp;nbsp;输出
	 */
	public boolean isWebMode() {
		return isWebMode;
	}

	/**
	 * 设置是否是web模式，在web模式下getString()方法会将null或空字符串转化成&amp;nbsp;输出
	 * 
	 * @param isWebMode web模式
	 * @return DataTable本身
	 */
	public DataTable setWebMode(boolean isWebMode) {
		this.isWebMode = isWebMode;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String[] columnNames = new String[getColumnCount()];
		for (int i = 0; i < columnNames.length; i++) {
			if (i != 0) {
				sb.append("\t");
			}
			sb.append(columns[i].getColumnName());
		}
		sb.append("\n");
		for (int i = 0; i < getRowCount(); i++) {
			for (int j = 0; j < getColumnCount(); j++) {
				if (j != 0) {
					sb.append("\t");
				}
				sb.append(StringUtil.javaEncode(getString(i, j)));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * @param columnName 字段名称
	 * @return 是否包含指定的字段名称
	 */
	public boolean containsColumn(String columnName) {
		return getColumnIndex(columnName) != -1;
	}

	/**
	 * 数据行遍历器
	 */
	@Override
	public Iterator<DataRow> iterator() {
		final DataTable dt = this;
		return new Iterator<DataRow>() {
			private int i = 0;

			@Override
			public boolean hasNext() {
				return dt.getRowCount() > i;
			}

			@Override
			public DataRow next() {
				return dt.getDataRow(i++);
			}

			@Override
			public void remove() {
				dt.deleteRow(i);
			}
		};
	}

	/**
	 * 获得字段名对应的字段的顺序
	 * 
	 * @param columnName 字段名
	 * @return 字段顺序
	 */
	public int getColumnIndex(String columnName) {
		int hash = CaseIgnoreMapx.caseIgnoreHash(columnName);
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].hash == hash) {
				return i;
			}
		}
		return -1;
	}
}
