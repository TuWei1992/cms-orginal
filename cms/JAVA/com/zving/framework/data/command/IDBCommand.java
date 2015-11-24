package com.zving.framework.data.command;

/**
 * 数据库指令
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public interface IDBCommand {

	/**
	 * @return 指令前缀
	 */
	public String getPrefix();

	/**
	 * @param dbType 数据库类型
	 * @return 指令对应的SQL数组。如果dbType对应的IDBType实例未实现本指令对应的toSQLArray()方法，则调用此方法获得指令对应的SQL数组。
	 */
	public String[] getDefaultSQLArray(String dbType);

	/**
	 * @param command JSON形式的指令
	 */
	public void parse(String command);// NO_UCD

	/**
	 * @return 指令转换为JSON字符串
	 */
	public String toJSON();// NO_UCD
}
