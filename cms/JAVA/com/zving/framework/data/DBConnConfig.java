package com.zving.framework.data;

import com.zving.framework.data.dbtype.MySQL;
import com.zving.framework.data.dbtype.Oracle;
import com.zving.framework.data.dbtype.Sybase;

/**
 * 数据库连接池配置信息类
 * 
 * @Author 王育春
 * @Date 2007-4-20
 * @Mail wyuch@midding.com
 */
public class DBConnConfig {

	public static final String ORACLE = Oracle.ID;

	public static final String DB2 = com.zving.framework.data.dbtype.DB2.ID;

	public static final String MYSQL = MySQL.ID;

	public static final String MSSQL = com.zving.framework.data.dbtype.MSSQL.ID;

	public static final String MSSQL2000 = com.zving.framework.data.dbtype.MSSQL2000.ID;

	public static final String SYBASE = Sybase.ID;

	public static final String HSQLDB = com.zving.framework.data.dbtype.HSQLDB.ID;

	public static final String DERBY_EMBEDDED = com.zving.framework.data.dbtype.DerbyEmbedded.ID;

	public static final String DERBY_SERVER = com.zving.framework.data.dbtype.DerbyServer.ID;

	/**
	 * JDBC驱动类名称
	 */
	public String DriverClass;

	/**
	 * JDNI名称
	 */
	public String JNDIName = null;

	/**
	 * 是否是一个JNDI池
	 */
	public boolean isJNDIPool = false;

	/**
	 * 最大连接数量，默认为1000
	 */
	public int MaxConnCount = 1000;

	/**
	 * 初始化时创建的连接数量，默认为5
	 */
	public int InitConnCount = 5;

	/**
	 * 己创建的连接数量
	 */
	public int ConnCount;

	/**
	 * 连接最长使用时间（单位为毫秒），如果连接不是长时连接，则超过此时间会抛出异常
	 */
	public int MaxConnUsingTime = 300000;// 以毫秒为单位

	/**
	 * 保持活动间隔（单位为毫秒），如果连接池中的连接超过此间隔，则会发一次测试语句给数据库以免数据库服务器将此连接自动关闭。
	 */
	public int KeepAliveInterval = 30000;// 一分钟检查一次连接是否己失效（数据库重启等原因造成）

	/**
	 * 数据库类型
	 */
	public String DBType;

	/**
	 * 数据库服务器地址
	 */
	public String DBServerAddress;

	/**
	 * 数据库服务器端口
	 */
	public int DBPort;

	/**
	 * JDBC URL，特殊情况（例如Oracle RAC）下需要使用这个
	 */
	public String ConnectionURL; // 指定连接串，可以用于oracleRAC

	/**
	 * 数据库名称
	 */
	public String DBName;

	/**
	 * 连接时使用的用户名
	 */
	public String DBUserName;

	/**
	 * 连接时使用的用户密码
	 */
	public String DBPassword;

	/**
	 * 测试表名
	 */
	public String TestTable;

	/**
	 * 连接池名称
	 */
	public String PoolName;

	/**
	 * 数据库字符集
	 */
	public String Charset;

	/**
	 * 是否是latin1字符集，如果在Oracle下是此字符集，则SQL及返回结果会自动转码
	 */
	public boolean isLatin1Charset;// 是否是latin1字符集，如果在Oracle下是此字符集，则SQL及返回结果必须转码

	/**
	 * @return 数据库服务器是否是Oracle
	 */
	public boolean isOracle() {
		return DBType.equalsIgnoreCase(ORACLE);
	}

	/**
	 * @return 数据库服务器是否是DB2
	 */
	public boolean isDB2() {
		return DBType.equalsIgnoreCase(DB2);
	}

	/**
	 * @return 数据库服务器是否是Mysql
	 */
	public boolean isMysql() {
		return DBType.equalsIgnoreCase(MYSQL);
	}

	/**
	 * @return 数据库服务器是否是SQLServer(2005以上)
	 */
	public boolean isSQLServer() {
		return DBType.equalsIgnoreCase(MSSQL);
	}

	/**
	 * @return 数据库服务器是否是SQLServer2000
	 */
	public boolean isSQLServer2000() {
		return DBType.equalsIgnoreCase(MSSQL2000);
	}

	/**
	 * @return 数据库服务器是否是Sybase ASE
	 */
	public boolean isSybase() {
		return DBType.equalsIgnoreCase(SYBASE);
	}

	/**
	 * @return 数据库服务器是否是HSQLDB
	 */
	public boolean isHSQLDB() {
		return DBType.equalsIgnoreCase(HSQLDB);
	}

	/**
	 * @return 数据库服务器是否是嵌入式的Derby
	 */
	public boolean isDerbyEmbedded() {
		return DBType.equalsIgnoreCase(DERBY_EMBEDDED);
	}

	/**
	 * @return 数据库服务器是否是服务器模式的Derby
	 */
	public boolean isDerbyServer() {
		return DBType.equalsIgnoreCase(DERBY_SERVER);
	}
}
