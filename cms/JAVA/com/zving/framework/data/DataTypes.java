package com.zving.framework.data;

/**
 * 数据类型，是对各个数据中数据类型的抽象。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-8
 */
public class DataTypes {
	/**
	 * Varchar
	 */
	public static final int STRING = 1;
	/**
	 * Blob
	 */
	public static final int BLOB = 2;
	/**
	 * BigDecimal
	 */
	public static final int BIGDECIMAL = 3;
	/**
	 * Decimal
	 */
	public static final int DECIMAL = 4;
	/**
	 * 浮点型
	 */
	public static final int FLOAT = 5;
	/**
	 * 双字节浮点型
	 */
	public static final int DOUBLE = 6;
	/**
	 * 长整型
	 */
	public static final int LONG = 7;
	/**
	 * 整型
	 */
	public static final int INTEGER = 8;
	/**
	 * 小整型
	 */
	public static final int SMALLINT = 9;
	/**
	 * Clob
	 */
	public static final int CLOB = 10;
	/**
	 * Bit
	 */
	public static final int BIT = 11;
	/**
	 * 日期时间
	 */
	public static final int DATETIME = 12;
	/**
	 * 对象
	 */
	public static final int OBJECT = 13;

	/**
	 * 输出成字符串
	 */
	public static String toString(int type) {
		switch (type) {
		case DATETIME:
			return "DateTime";
		case STRING:
			return "String";
		case BLOB:
			return "BLOB";
		case BIGDECIMAL:
			return "BigDecimal";
		case DECIMAL:
			return "Decimal";
		case FLOAT:
			return "Float";
		case DOUBLE:
			return "Double";
		case LONG:
			return "Long";
		case INTEGER:
			return "Integer";
		case SMALLINT:
			return "SmallInt";
		case CLOB:
			return "Clob";
		case BIT:
			return "Bit";
		default:
			return "Unknown";
		}
	}
}
