package com.zving.framework.data;

import java.io.Serializable;
import java.util.HashMap;

import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.json.JSON;
import com.zving.framework.json.JSONAware;

/**
 * 数据字段，表示DataTable中的一个列
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2006-7-6
 */
public final class DataColumn implements Serializable, Cloneable, JSONAware {
	private static final long serialVersionUID = 1L;

	private String columnName;

	private int columnType;

	private boolean isAllowNull = true;// 暂未起用

	private String dateFormat = null;

	protected int hash;

	/**
	 * 空构造器
	 */
	public DataColumn() {
	}

	/**
	 * 克隆
	 */
	@Override
	public Object clone() {
		return new DataColumn(columnName, columnType);
	}

	/**
	 * 构造器
	 * 
	 * @param columnName 字段名
	 * @param columnType 数据类型
	 * @see com.zving.framework.data.DataTypes
	 */
	public DataColumn(String columnName, int columnType) {
		this.columnName = columnName;
		this.columnType = columnType;
		hash = CaseIgnoreMapx.caseIgnoreHash(columnName);
	}

	/**
	 * 构造器
	 * 
	 * @param columnName 字段名
	 * @param columnType 数据类型
	 * @param allowNull 是否允许为空
	 * @see com.zving.framework.data.DataTypes
	 */
	public DataColumn(String columnName, int columnType, boolean allowNull) {// NO_UCD
		this.columnName = columnName;
		this.columnType = columnType;
		isAllowNull = allowNull;
		hash = CaseIgnoreMapx.caseIgnoreHash(columnName);
	}

	/**
	 * @return 字段名称
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * 设置字段名称
	 * 
	 * @param columnName 字段名称
	 * @return 实例本身
	 */
	public DataColumn setColumnName(String columnName) {
		this.columnName = columnName;
		hash = CaseIgnoreMapx.caseIgnoreHash(columnName);
		return this;
	}

	/**
	 * @return 字段数据类型
	 */
	public int getColumnType() {
		return columnType;
	}

	/**
	 * 设置字段数据类型
	 * 
	 * @param columnType 字段数据类型
	 * @return 实例本身
	 */
	public DataColumn setColumnType(int columnType) {
		this.columnType = columnType;
		return this;
	}

	/**
	 * @return 是否允许为空
	 */
	public boolean isAllowNull() {
		return isAllowNull;
	}

	/**
	 * 设置是否允许为空
	 * 
	 * @param isAllowNull 是否允许为空
	 * @return 实例本身
	 */
	public DataColumn setAllowNull(boolean isAllowNull) {
		this.isAllowNull = isAllowNull;
		return this;
	}

	/**
	 * @return 字段的日期格式
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * 设置字段的日期格式，字段的日期格式会影响相应列的getString()方法的输出结果
	 * 
	 * @param dateFormat 日期格式
	 * @return 实例本身
	 */
	public DataColumn setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}

	/**
	 * 输出成JSON字符串
	 */
	@Override
	public String toJSONString() {
		HashMap<String, Object> mapx = new HashMap<String, Object>();
		mapx.put("Name", columnName);
		mapx.put("Type", columnType);
		return JSON.toJSONString(mapx);
	}
}
