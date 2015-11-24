package com.zving.framework.orm;

/**
 * DAO中单个字段的描述信息<br>
 * 作者：王育春<br>
 * 日期：2006-7-3<br>
 * 邮件：wyuch@zving.com<br>
 */
public class DAOColumn implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private int columnType;

	private String columnName;

	private int length;

	private int precision;

	private boolean mandatory;

	private boolean isPrimaryKey;

	/**
	 * 构造器
	 * 
	 * @param name 字段名
	 * @param type 字段类型
	 * @param length 字段长度
	 * @param precision 字段精度
	 * @param mandatory 是否必填
	 * @param isPrimaryKey 是否是主键
	 */
	public DAOColumn(String name, int type, int length, int precision, boolean mandatory, boolean isPrimaryKey) {
		this.columnType = type;
		this.columnName = name;
		this.length = length;
		this.precision = precision;
		this.mandatory = mandatory;
		this.isPrimaryKey = isPrimaryKey;
	}

	/**
	 * 获取字段名
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * 获取字段类型
	 */
	public int getColumnType() {
		return columnType;
	}

	/**
	 * 是否是主键
	 */
	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	/**
	 * 获取字段长度
	 */
	public int getLength() {
		return length;
	}

	/**
	 * 获取字段精度
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * 是否是非空字段
	 */
	public boolean isMandatory() {
		return mandatory;
	}
}
