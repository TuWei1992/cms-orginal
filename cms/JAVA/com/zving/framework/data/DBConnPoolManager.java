package com.zving.framework.data;

import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.Config;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 连接池管理类,同时管理多个连接池
 * 
 * @Author 王育春
 * @Date 2007-5-30
 * @Mail wyuch@zving.com
 */
public class DBConnPoolManager {
	public static final String DEFAULT_POOLNAME = "Default";
	protected static ConcurrentMapx<String, DBConnPool> poolMap = new ConcurrentMapx<String, DBConnPool>();
	private static ReentrantLock lock = new ReentrantLock();
	private static ThreadLocal<String> threadCurrentPool;

	public static DBConn getConnection() {
		return getConnection(null, false);
	}

	public static DBConn getConnection(boolean bLongTimeFlag) {// NO_UCD
		return getConnection(null, bLongTimeFlag);
	}

	public static DBConn getConnection(String poolName) {
		return getConnection(poolName, false);
	}

	/**
	 * @return 默认连接池的配置信息
	 */
	public static DBConnConfig getDBConnConfig() {
		return getDBConnConfig(null);
	}

	/**
	 * @param poolName 连接池名称
	 * @return 连接池名称对应的连接池，如果找不到则返回null
	 */
	public static DBConnConfig getDBConnConfig(String poolName) {
		if (StringUtil.isNull(poolName)) {
			if (threadCurrentPool != null) {
				poolName = threadCurrentPool.get();
			}
			if (StringUtil.isNull(poolName)) {
				poolName = DEFAULT_POOLNAME;
			}
		}
		DBConnPool pool = poolMap.get(poolName);
		if (pool == null) {
			lock.lock();
			try {
				pool = poolMap.get(poolName);
				if (pool == null) {
					if (Config.getValue("Database." + poolName + ".Type") != null) {
						pool = new DBConnPool(createConnConfig(poolName, Config.getMapx()));
						poolMap.put(poolName, pool);
					} else {
						throw new RuntimeException("DB Connection Pool not found:" + poolName);
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return pool.getConfig();
	}

	/**
	 * @param poolName 连接池名称
	 * @param isLongTimeOperation 是否是长时操作
	 * @return 指定连接池中的JDBC连接
	 */
	public static DBConn getConnection(String poolName, boolean isLongTimeOperation) {
		return getConnection(poolName, isLongTimeOperation, true);
	}

	/**
	 * @param poolName 连接池名称
	 * @param isLongTimeOperation 是否是长时操作
	 * @param isCurrentThreadConnection 是否使用当前线程中的连接，如果为true且当前线程中有阻塞形连接，则使用当前线程中的连接
	 * @return 指定连接池中的JDBC连接
	 */
	public static DBConn getConnection(String poolName, boolean isLongTimeOperation, boolean isCurrentThreadConnection) {
		if (StringUtil.isNull(poolName)) {
			if (threadCurrentPool != null) {
				poolName = threadCurrentPool.get();
			}
			if (StringUtil.isNull(poolName)) {
				poolName = DEFAULT_POOLNAME;
			}
		}
		if (isCurrentThreadConnection) {
			DBConn conn = BlockingTransaction.getCurrentThreadConnection();
			if (conn != null && conn.dbConfig.PoolName.equals(poolName)) {
				return conn;// 如果存在阻塞型事务，并且其中的连接的连接池名和当前申请的连接池名称一致，则直接返回该连接，以保证整个处理过程中能够查询到正确的数据。
			}
		}
		DBConnPool pool = poolMap.get(poolName);
		if (pool == null) {
			lock.lock();
			try {
				pool = poolMap.get(poolName);
				if (pool == null) {
					if (Config.getValue("Database." + poolName + ".Type") != null) {
						pool = new DBConnPool(createConnConfig(poolName, Config.getMapx()));
						poolMap.put(poolName, pool);
					} else {
						throw new RuntimeException("DB Connection Pool not found:" + poolName);
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return pool.getConnection(isLongTimeOperation);
	}

	/**
	 * @param poolName 连接池名称
	 * @return 从map读取连接池配置信息
	 */
	public static DBConnConfig createConnConfig(String poolName, Mapx<String, String> map) {
		DBConnConfig dcc = new DBConnConfig();
		dcc.PoolName = poolName;
		dcc.DBType = map.get("Database." + dcc.PoolName + ".Type");
		dcc.JNDIName = map.get("Database." + dcc.PoolName + ".JNDIName");
		dcc.isLatin1Charset = "true".equalsIgnoreCase(map.get("Database." + dcc.PoolName + ".isLatin1Charset"));
		if (StringUtil.isNotEmpty(dcc.JNDIName)) {
			dcc.isJNDIPool = true;
		} else {
			dcc.DBServerAddress = map.get("Database." + dcc.PoolName + ".ServerAddress");
			dcc.ConnectionURL = map.get("Database." + dcc.PoolName + ".ConnectionURL");
			dcc.DriverClass = map.get("Database." + dcc.PoolName + ".DriverClass");
			dcc.DBName = map.get("Database." + dcc.PoolName + ".Name");
			dcc.DBUserName = map.get("Database." + dcc.PoolName + ".UserName");
			dcc.DBPassword = map.get("Database." + dcc.PoolName + ".Password");
			dcc.TestTable = map.get("Database." + dcc.PoolName + ".TestTable");
			if (StringUtil.isEmpty(dcc.DBType)) {
				throw new DatabaseException("DB.Type not found");
			}
			if (StringUtil.isEmpty(dcc.ConnectionURL)) {
				if (StringUtil.isEmpty(dcc.DBServerAddress)) {
					throw new DatabaseException("DB.ServerAddress not found");
				}
				if (StringUtil.isEmpty(dcc.DBName)) {
					throw new DatabaseException("DB.Name not found");
				}
				if (StringUtil.isEmpty(dcc.DBUserName)) {
					throw new DatabaseException("DB.UserName not found");
				}
				if (dcc.DBPassword == null) {// 可能为空
					throw new DatabaseException("DB.Password not found");
				}
			} else {
				dcc.ConnectionURL = dcc.ConnectionURL.trim();
			}
			String s = map.get("Database." + dcc.PoolName + ".InitConnCount");
			try {
				dcc.InitConnCount = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				dcc.InitConnCount = 0;
				LogUtil.warn(s + " is invalid DB.InitConnCount value,will use 0");
			}
			s = map.get("Database." + dcc.PoolName + ".MaxConnCount");
			try {
				dcc.MaxConnCount = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				dcc.MaxConnCount = 20;
				LogUtil.warn(s + " is invalid DB.MaxConnCount value,will use 20");
			}
			s = map.get("Database." + dcc.PoolName + ".Port");
			if (StringUtil.isNotEmpty(s)) {
				try {
					dcc.DBPort = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					IDBType t = DBTypeService.getInstance().get(dcc.DBType);
					if (t == null) {
						throw new DatabaseException("Unknow DB Type:" + dcc.DBType);
					}
					dcc.DBPort = t.getDefaultPort();
					LogUtil.warn(s + " is invalid DB.Port value,will use default value");
				}
			}
		}
		if (dcc.InitConnCount < 1) {
			dcc.InitConnCount = 1;
		}
		return dcc;
	}

	/**
	 * 添加一个数据库连接池
	 * 
	 * @param dcc 数据库连接池配置
	 */
	public static void addPool(DBConnConfig dcc) {
		poolMap.put(dcc.PoolName, new DBConnPool(dcc));
	}

	/**
	 * @param poolName 连接池名称
	 * @return 对应的连接池实例，如果未找到则返回null
	 */
	public static DBConnPool getPool(String poolName) {
		return poolMap.get(poolName);
	}

	/**
	 * @param poolName 连接池名称
	 * @return 删除掉的连接池实例，如果没有对应的连接池，则返回null
	 */
	public static DBConnPool removePool(String poolName) {
		return poolMap.remove(poolName);
	}

	/**
	 * 将当前线程中的所有的数据库操作的目标设置到指定的连接池
	 * 
	 * @param poolName 连接池名称
	 */
	public static void setThreadCurrentPool(String poolName) {// NO_UCD
		if (poolName == null) {
			return;
		}
		if (threadCurrentPool == null) {
			lock.lock();
			try {
				if (threadCurrentPool == null) {
					threadCurrentPool = new ThreadLocal<String>();
				}
			} finally {
				lock.unlock();
			}
		}
		threadCurrentPool.set(poolName);
	}

	/**
	 * 销毁所有连接池，关闭所有连接。
	 */
	public static void destory() {
		for (DBConnPool pool : poolMap.values()) {
			pool.clear();
		}
		poolMap.clear();
		poolMap = null;
	}
}
