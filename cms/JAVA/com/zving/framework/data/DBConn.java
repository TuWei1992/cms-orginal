package com.zving.framework.data;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.zving.framework.Config;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.data.nativejdbc.CommonsDbcpNativeJdbcExtractor;
import com.zving.framework.data.nativejdbc.JBossNativeJdbcExtractor;
import com.zving.framework.data.nativejdbc.WebLogicNativeJdbcExtractor;
import com.zving.framework.data.nativejdbc.WebSphereNativeJdbcExtractor;
import com.zving.framework.thirdparty.commons.ArrayUtils;

/**
 * 数据库连接，调用本类的close()方法时不实际关闭连接，仅将连接释放回连接池。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-8
 */
public class DBConn implements Connection {
	protected boolean longTimeFlag = false;

	protected Connection conn;

	protected boolean isUsing = false;

	protected StackTraceElement[] callerStackTrace = null;

	protected String lastSQL = null;

	protected int connID = 0;

	protected long lastWarnTime;// 上次警告时间'

	protected long lastSuccessExecuteTime = System.currentTimeMillis();

	protected boolean isBlockingTransactionStarted;// 是否处于阻塞型事务之中

	protected DBConnConfig dbConfig;

	protected DBConn() {
	}

	public Connection getPhysicalConnection() {
		if (dbConfig.isJNDIPool) {
			try {
				if (Config.isTomcat()) {
					return CommonsDbcpNativeJdbcExtractor.doGetNativeConnection(conn);
				} else if (Config.isWeblogic()) {
					return WebLogicNativeJdbcExtractor.doGetNativeConnection(conn);
				} else if (Config.isWebSphere()) {
					return WebSphereNativeJdbcExtractor.doGetNativeConnection(conn);
				} else {// JBOSS
					return JBossNativeJdbcExtractor.doGetNativeConnection(conn);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException {
		return conn.getHoldability();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	@Override
	public int getTransactionIsolation() throws SQLException {
		return conn.getTransactionIsolation();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		conn.clearWarnings();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#close()
	 */
	@Override
	public void close() throws SQLException {// 不真正关闭连接
		isUsing = false;
		setAutoCommit(true);
		if (dbConfig.isJNDIPool) {
			conn.close();
		}
	}

	public void closeReally() throws SQLException {// 真正关闭
		DBConnPool pool = DBConnPoolManager.poolMap.get(dbConfig.DBName + ".");
		removeConnID(connID);
		if (pool != null) {
			DBConn[] arr = ArrayUtils.removeElement(pool.conns, this);
			DBConn[] arr2 = new DBConn[arr.length + 1];
			for (int i = 0; i < arr.length; i++) {
				arr2[i] = arr[i];
			}
			pool.conns = arr2;
			pool.getConfig().ConnCount--;
		}
		conn.close();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#commit()
	 */
	@Override
	public void commit() throws SQLException {
		if (!conn.getAutoCommit()) {
			conn.commit();
		}
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#rollback()
	 */
	@Override
	public void rollback() throws SQLException {
		conn.rollback();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#getAutoCommit()
	 */
	@Override
	public boolean getAutoCommit() throws SQLException {
		return conn.getAutoCommit();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return conn.isClosed();// 总是处于打开状态
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() throws SQLException {
		return conn.isReadOnly();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#setHoldability(int)
	 */
	@Override
	public void setHoldability(int holdability) throws SQLException {
		conn.setHoldability(holdability);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		conn.setTransactionIsolation(level);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (conn.getAutoCommit() != autoCommit) {
			conn.setAutoCommit(autoCommit);
			// 避免Sybase下多次调用setAutoCommit出现SET CHAINED command not allowed
			// within multi-statement transaction的错误
		}
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		conn.setReadOnly(readOnly);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#getCatalog()
	 */
	@Override
	public String getCatalog() throws SQLException {
		return conn.getCatalog();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	@Override
	public void setCatalog(String catalog) throws SQLException {
		conn.setCatalog(catalog);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#getMetaData()
	 */
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return conn.getMetaData();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return conn.getWarnings();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#setSavepoint()
	 */
	@Override
	public Savepoint setSavepoint() throws SQLException {
		return conn.setSavepoint();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		conn.releaseSavepoint(savepoint);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		conn.rollback(savepoint);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#createStatement()
	 */
	@Override
	public Statement createStatement() throws SQLException {
		return conn.createStatement();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return conn.createStatement(resultSetType, resultSetConcurrency);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#getTypeMap()
	 */
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return conn.getTypeMap();
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		conn.setTypeMap(map);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	@Override
	public String nativeSQL(String sql) throws SQLException {
		return conn.nativeSQL(sql);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return conn.prepareCall(sql);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return conn.prepareStatement(sql, autoGeneratedKeys);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		lastSQL = sql;
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return conn.prepareStatement(sql, columnIndexes);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return conn.setSavepoint(name);
	}

	/*
	 * （非 Javadoc）
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return conn.prepareStatement(sql, columnNames);
	}

	public long getLastSuccessExecuteTime() {
		return lastSuccessExecuteTime;
	}

	public void setLastSuccessExecuteTime(long lastSuccessExecuteTime) {
		this.lastSuccessExecuteTime = lastSuccessExecuteTime;
	}

	public DBConnConfig getDBConfig() {
		return dbConfig;
	}

	public void setPoolName(DBConnConfig dbcc) {
		dbConfig = dbcc;
	}

	/**
	 * 兼容1.2以前的写法
	 * 
	 * @deprecated
	 */
	@Deprecated
	public String getDBType() {
		return dbConfig.DBType;
	}

	private static ConcurrentMapx<String, String> IDMap = new ConcurrentMapx<String, String>();

	/**
	 * 连接的唯一ID，有些数据库例如Sybase需要根据这个清除临时表
	 */
	public static int getConnID() {
		for (int i = 1; i <= 2000; i++) {// 最多2000个连接
			if (!IDMap.containsKey(i + "")) {
				IDMap.put(i + "", "1");
				return i;
			}
		}
		return 0;
	}

	public static void removeConnID(int id) {
		IDMap.remove(id + "");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return conn.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return conn.isWrapperFor(iface);
	}

	@Override
	public Clob createClob() throws SQLException {
		return conn.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return conn.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return conn.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return conn.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return conn.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		conn.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		conn.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return conn.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return conn.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return conn.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return conn.createStruct(typeName, attributes);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
}
