package com.zving.framework.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.zving.framework.Config;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.utility.LogUtil;

/**
 * 数据库连接池
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2006-1-8
 */
public class DBConnPool {
	private static ConcurrentMapx<String, String> jndiMap = new ConcurrentMapx<String, String>(1000);
	private DBConnConfig dcc;
	private ReentrantLock lock = new ReentrantLock();
	protected DBConn[] conns;

	/**
	 * 构造器
	 * 
	 * @param dcc 连接池配置信息
	 */
	public DBConnPool(DBConnConfig dcc) {
		this.dcc = dcc;
		if (DBConnPoolManager.getPool(dcc.PoolName) != null) {
			throw new DatabaseException("DB Connection Pool is exist:" + dcc.PoolName);
		}
		fillInitConn();
	}

	/**
	 * 按InitConnCount指定的数量创建连接
	 */
	private void fillInitConn() {
		if (!dcc.isJNDIPool) {
			conns = new DBConn[dcc.MaxConnCount];
			try {
				for (int i = 0; i < dcc.InitConnCount; i++) {
					conns[i] = createConnection(dcc, false);
					conns[i].isUsing = false;
				}
				dcc.ConnCount = dcc.InitConnCount;
				LogUtil.info("----" + dcc.PoolName + " init " + dcc.InitConnCount + " connection");
			} catch (Exception e) {
				LogUtil.warn("----" + dcc.PoolName + "init Connections failed");
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return 所有连接
	 */
	public DBConn[] getDBConns() {
		return conns;
	}

	/**
	 * 关闭连接池中的所有连接并清空
	 */
	public void clear() {
		if (conns == null) {
			return;
		}
		for (int i = 0; i < conns.length; i++) {
			if (conns[i] != null) {
				try {
					conns[i].conn.close();
					conns[i] = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		dcc.ConnCount = 0;
	}

	/**
	 * @return 连接池对应的数据库类型
	 */
	public String getDBType() {
		return dcc.DBType;
	}

	/**
	 * @return 连接池配置信息
	 */
	public DBConnConfig getConfig() {
		return dcc;
	}

	/**
	 * @param isLongTimeOperation 是否将连接标记为长时间占用
	 * @return 返回连接池中的一个连接，如果连接池中的连接都被占用，则新创建一个连接。
	 */
	public DBConn getConnection(boolean isLongTimeOperation) {
		if (dcc.isJNDIPool) {
			return getJNDIPoolConnection(dcc);
		}
		long now = System.currentTimeMillis();
		DBConn conn = null;
		lock.lock();
		try {
			for (int i = 0; i < dcc.ConnCount; i++) {
				conn = conns[i];
				if (conn == null) {
					continue;
				}
				if (conn.isUsing) {
					if (!conn.longTimeFlag) {
						if (now - conn.lastSuccessExecuteTime > dcc.MaxConnUsingTime) {
							LogUtil.error(dcc.PoolName
									+ ":connection timeout,will close connection automatical,there is last sql and invoke stack:");
							LogUtil.error("Last SQL:" + conn.lastSQL);
							LogUtil.warn(getCallerStack(conn));
							DataAccess.log(conn.lastSuccessExecuteTime, "Timeout:" + conn.lastSQL, null);
							final DBConn conn2 = conn;
							new Thread() {// 另开线程关闭，以免close()操作阻塞其他线程
								@Override
								public void run() {
									try {
										if (!conn2.conn.getAutoCommit()) {
											conn2.conn.rollback();
										}
									} catch (SQLException e) {
										e.printStackTrace();
									} finally {
										try {
											conn2.conn.close();// 先关闭，再创建新的
										} catch (SQLException e) {
											e.printStackTrace();
										}
									}
								}
							}.start();
							try {
								conn = createConnection(dcc, isLongTimeOperation);
								conns[i] = conn;
								LogUtil.info(dcc.PoolName + ":create a new connection,total is " + dcc.ConnCount + " line 215");
								setCaller(conn);
								conn.lastSuccessExecuteTime = now;
								return conn;
							} catch (Exception e) {
								e.printStackTrace();
								throw new DatabaseException("DBConnPoolImpl," + dcc.PoolName + "create new connection failed:"
										+ e.getMessage());
							}
						}
					} else if (now - conn.lastSuccessExecuteTime > 4 * dcc.MaxConnUsingTime && now - conn.lastWarnTime > 300000) {
						LogUtil.warn(dcc.PoolName + ":connection used " + (now - conn.lastSuccessExecuteTime)
								+ " ms,there is invoke stack:");
						LogUtil.warn(getCallerStack(conn));
						conn.lastWarnTime = now;
					}
				} else if (!conn.isBlockingTransactionStarted) {// 阻塞型不可用
					conn.longTimeFlag = isLongTimeOperation;
					conn.isUsing = true;
					setCaller(conn);
					// 检查连接是否己失效，若己失效则重新连接
					keepAlive(conn);
					return conn;
				}
			}
			if (dcc.ConnCount < dcc.MaxConnCount) {
				try {
					conn = createConnection(dcc, isLongTimeOperation);
					conns[dcc.ConnCount] = conn;
					dcc.ConnCount++;
					LogUtil.info(dcc.PoolName + ":create a new connection,total is " + dcc.ConnCount + " line 246");
					setCaller(conn);
					conn.lastSuccessExecuteTime = now;
					return conn;
				} catch (Exception e) {
					e.printStackTrace();
					throw new DatabaseException("DBConnPoolImpl," + dcc.PoolName + ":create new connection failed:" + e.getMessage());
				}
			} else {
				throw new DatabaseException("DBConnPoolImpl," + dcc.PoolName + ":all connection is using!");
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 保持一个连接存活，避免长时间不操作被数据库服务器自动关闭
	 * 
	 * @param conn
	 */
	private void keepAlive(DBConn conn) {
		if (conn == null) {
			return;
		}
		if (System.currentTimeMillis() - conn.getLastSuccessExecuteTime() > dcc.KeepAliveInterval) {
			PreparedStatement stmt = null;
			String sql = "select 1 from " + dcc.TestTable + " where 1=2";
			ResultSet rs = null;
			try {
				stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery();
			} catch (SQLException e) {
				try {
					conn.conn.close();
				} catch (SQLException e1) {
				}
				try {
					conn.conn = createConnection(dcc, false).conn;
				} catch (Exception e1) {
					LogUtil.error(dcc.PoolName + ":Reconnection is failed:" + e1.getMessage());
				}
			} finally {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (stmt != null) {
						stmt.close();
						stmt = null;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 保持池中所有连接存活，避免长时间不操作被数据库服务器自动关闭
	 */
	public void keepAlive() {
		lock.lock();
		try {
			int count = 0;
			if (conns == null) {
				return;
			}
			for (int i = 0; i < conns.length; i++) {
				DBConn conn = conns[i];
				if (conn == null || conn.isUsing) {
					continue;
				}
				if (count < dcc.InitConnCount) {
					count++;
					keepAlive(conn);
					conn.lastSuccessExecuteTime = System.currentTimeMillis();
				} else {// 如果空闲的连接数超过InitConnCount(默认是5个)，则关掉
					conns[i] = null;
					dcc.ConnCount--;
					try {
						conn.conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @param dcc 连接池配置信息
	 * @return 根据连接池配置信息返回一个JNDI连接
	 */
	private static DBConn getJNDIPoolConnection(DBConnConfig dcc) {
		int connID = DBConn.getConnID();
		try {
			Context ctx = new InitialContext();
			Connection conn = null;
			if (Config.isTomcat()) {
				ctx = (Context) ctx.lookup("java:comp/env");
				DataSource ds = (DataSource) ctx.lookup(dcc.JNDIName);
				conn = ds.getConnection();
			} else if (Config.isJboss()) {
				Hashtable<String, String> environment = new Hashtable<String, String>();
				environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
				environment.put(Context.URL_PKG_PREFIXES, "org.jboss.naming.client ");
				environment.put(Context.PROVIDER_URL, "jnp://127.0.0.1:1099");
				ctx = new InitialContext(environment);
				DataSource ds = (DataSource) ctx.lookup("java:" + dcc.JNDIName);
				conn = ds.getConnection();
			} else {
				DataSource ds = (DataSource) ctx.lookup(dcc.JNDIName);
				conn = ds.getConnection();
			}
			IDBType t = DBTypeService.getInstance().get(dcc.DBType);
			DBConn dbconn = new DBConn();
			dbconn.conn = conn;
			dbconn.dbConfig = dcc;
			dbconn.connID = connID;
			if (!jndiMap.containsKey(conn.toString())) {
				t.afterConnectionCreate(dbconn);
				jndiMap.put(conn.toString(), "");
			}
			return dbconn;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.warn("Find JNDI connection pool failed:" + e.getMessage());
			DBConn.removeConnID(connID);
		}
		return null;
	}

	/**
	 * @param dcc 连接池配置信息
	 * @param isLongTimeOperation 是否是长时间操作
	 * @return 创建好的JDBC连接
	 * @throws Exception
	 */
	public static DBConn createConnection(DBConnConfig dcc, boolean isLongTimeOperation) throws Exception {
		Connection conn = null;
		if (dcc.isJNDIPool) {
			return getJNDIPoolConnection(dcc);
		} else {
			IDBType t = DBTypeService.getInstance().get(dcc.DBType);
			if (t == null) {
				LogUtil.error("Database type is not supported:" + dcc.DBType);
			}
			conn = t.createConnection(dcc);
		}
		DBConn dbconn = new DBConn();
		dbconn.conn = conn;
		dbconn.isUsing = true;
		dbconn.longTimeFlag = isLongTimeOperation;
		dbconn.dbConfig = dcc;
		dbconn.connID = DBConn.getConnID();
		return dbconn;
	}

	/**
	 * 设置连接的调用堆栈
	 */
	private void setCaller(DBConn conn) {
		conn.callerStackTrace = new Throwable().getStackTrace();
	}

	/**
	 * 获取连接的调用堆栈信息
	 */
	private String getCallerStack(DBConn conn) {
		StackTraceElement[] trace = conn.callerStackTrace;
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : trace) {
			sb.append("\tat " + element);
		}
		return sb.toString();
	}

	/**
	 * 请使用DBConnPoolManager.getConnection()替代
	 */
	@Deprecated
	public static DBConn getConnection() {
		return DBConnPoolManager.getConnection();
	}

	/**
	 * 请使用DBConnPoolManager.getConnection(poolName)替代
	 */
	@Deprecated
	public static DBConn getConnection(String poolName) {
		return DBConnPoolManager.getConnection(poolName);
	}

	/**
	 * 请使用DBConnPoolManager.getConnection(poolName,isLongTimeOperation,isCurrentThreadConnection)替代
	 */
	@Deprecated
	public static DBConn getConnection(String poolName, boolean isLongTimeOperation, boolean isCurrentThreadConnection) {
		return DBConnPoolManager.getConnection(poolName, isLongTimeOperation, isCurrentThreadConnection);
	}

	/**
	 * 请使用DBConnPoolManager.getDBConnConfig()替代
	 */
	@Deprecated
	public static DBConnConfig getDBConnConfig() {
		return DBConnPoolManager.getDBConnConfig();
	}

	/**
	 * 请使用DBConnPoolManager.getDBConnConfig(poolName)替代
	 */
	@Deprecated
	public static DBConnConfig getDBConnConfig(String poolName) {
		return DBConnPoolManager.getDBConnConfig(poolName);
	}

}
