/*
 * 创建日期 2005-7-15 
 * 作者：王育春 
 * 邮箱:wangyc@zving.com
 */
package com.zving.framework.data;

import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.config.LogSQL;
import com.zving.framework.config.RWSpliting;
import com.zving.framework.config.ReadOnlyDB;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.exception.AlterException;
import com.zving.framework.data.exception.CloseException;
import com.zving.framework.data.exception.CommitException;
import com.zving.framework.data.exception.CreateException;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.data.exception.DeleteException;
import com.zving.framework.data.exception.DropException;
import com.zving.framework.data.exception.InsertException;
import com.zving.framework.data.exception.RollbackException;
import com.zving.framework.data.exception.SetAutoCommitException;
import com.zving.framework.data.exception.SetParamException;
import com.zving.framework.data.sql.SelectSQLParser;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.action.AfterSQLExecutedAction;
import com.zving.framework.orm.DAO;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringFormat;

/**
 * 数据库存取器，是对数据库连接的简单封装。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2015-7-6
 */
public class DataAccess {// NO_UCD
	/**
	 * 数据库连接
	 */
	DBConn conn;
	/**
	 * 只读数据库连接（在读写分离启用时有值）
	 */
	DBConn readOnlyConn;
	/**
	 * 是否在本实例的所有操作中停用读写分离
	 */
	boolean rwsplitingDisabled = false;

	/**
	 * 使用默认连接池中的连接构造实例
	 */
	public DataAccess() {
	}

	/**
	 * 使用指定的连接构造实例
	 */
	public DataAccess(DBConn conn) {
		this.conn = conn;
	}

	/**
	 * 即例在配置文件中启用了读写分离，在本实例中也不使用读写分离，读写操作都在主数据库上进行。
	 */
	public void nonuseRWSpliting() {
		rwsplitingDisabled = true;
	}

	/**
	 * @return 内部使用的JDBC连接
	 */
	public DBConn getConnection() {
		if (conn == null) {
			conn = DBConnPoolManager.getConnection();
		}
		return conn;
	}

	/**
	 * 设置事务提交模式
	 * 
	 * @param autoCommit 是否自动提交
	 * @return 实例本身
	 */
	public DataAccess setAutoCommit(boolean autoCommit) {
		if (conn == null) {
			conn = DBConnPoolManager.getConnection();
		}
		try {
			conn.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			throw new SetAutoCommitException(e);
		}
		return this;
	}

	/**
	 * 提交事务
	 */
	public void commit() {
		if (conn == null) {
			return;
		}
		try {
			conn.commit();
		} catch (SQLException e) {
			throw new CommitException(e);
		}
	}

	/**
	 * 回滚事务
	 */
	public void rollback() {
		if (conn == null) {
			return;
		}
		try {
			conn.rollback();
		} catch (SQLException e) {
			throw new RollbackException(e);
		}
	}

	/**
	 * 关闭连接（实际上是将连接释放回连接池）
	 */
	public void close() {
		if (readOnlyConn != null && conn != readOnlyConn) {
			try {
				readOnlyConn.close();
			} catch (SQLException e) {
				throw new CloseException(e);
			}
		}
		
		if (conn == null || conn == BlockingTransaction.getCurrentThreadConnection()) {
			return;
		}
		
		try {
			if(!conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			throw new CloseException(e);
		}
	}

	/**
	 * 将查询器中的变量设置到PreparedStatement中
	 * 
	 * @param stmt PreparedStatement
	 * @param q 查询器
	 * @param conn 数据库连接
	 */
	public static void setParams(PreparedStatement stmt, QueryBuilder q, DBConn conn) {
		// qb.checkParams();
		IDBType db = DBTypeService.getInstance().get(conn.getDBConfig().DBType);
		try {
			ArrayList<ArrayList<Object>> batches = null;
			if (q.isBatchMode()) {// 批量模式，以提高性能
				batches = q.getBatches();
				for (int k = 0; k < batches.size(); k++) {
					ArrayList<Object> list = batches.get(k);
					setParam(list, stmt, db, conn);
					stmt.addBatch();
				}
			} else {
				ArrayList<Object> list = q.getParams();
				setParam(list, stmt, db, conn);
			}
		} catch (SQLException e) {
			throw new SetParamException(e);
		}
	}

	private static void setParam(ArrayList<Object> list, PreparedStatement stmt, IDBType db, DBConn conn) throws SQLException {
		for (int i = 1; i <= list.size(); i++) {
			Object o = list.get(i - 1);
			if (o == null) {
				stmt.setNull(i, java.sql.Types.VARCHAR);
			} else if (o instanceof Byte) {
				stmt.setByte(i, ((Byte) o).byteValue());
			} else if (o instanceof Short) {
				stmt.setShort(i, ((Short) o).shortValue());
			} else if (o instanceof Integer) {
				stmt.setInt(i, ((Integer) o).intValue());
			} else if (o instanceof Long) {
				stmt.setLong(i, ((Long) o).longValue());
			} else if (o instanceof Float) {
				stmt.setFloat(i, ((Float) o).floatValue());
			} else if (o instanceof Double) {
				stmt.setDouble(i, ((Double) o).doubleValue());
			} else if (o instanceof Date) {
				stmt.setTimestamp(i, new java.sql.Timestamp(((java.util.Date) o).getTime()));
			} else if (o instanceof String) {
				String str = (String) o;
				if (conn.getDBConfig().isLatin1Charset && conn.getDBConfig().isOracle()) {// Oracle必须特别处理
					try {
						str = new String(str.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else {
					stmt.setString(i, str);
				}
			} else if (o instanceof Clob) {
				db.setClob(conn, stmt, i, o);
			} else if (o instanceof byte[]) {
				db.setBlob(conn, stmt, i, (byte[]) o);
			} else {
				stmt.setObject(i, o);
			}
		}
	}

	/**
	 * 查询DataTable,本方法等同于executeDataTable()
	 * 
	 * @param q 查询器
	 * @return 查询器执行后获得的DataTable
	 */
	public DataTable fetch(QueryBuilder q) {
		return executeDataTable(q);
	}

	/**
	 * 查询DataTable
	 * 
	 * @param q 查询器
	 * @return 查询器执行后获得的DataTable
	 */
	public DataTable executeDataTable(QueryBuilder q) {
		prepareReadOnlyConn(null);
		long start = System.currentTimeMillis();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DataTable dt = null;
		String sql = q.getSQL();
		try {
			boolean latin1Flag = readOnlyConn.getDBConfig().isLatin1Charset && readOnlyConn.getDBConfig().isOracle();
			if (latin1Flag) {
				try {
					sql = new String(sql.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			stmt = readOnlyConn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			setParams(stmt, q, readOnlyConn);
			rs = stmt.executeQuery();
			dt = new DataTable(readOnlyConn, rs, latin1Flag);
			readOnlyConn.setLastSuccessExecuteTime(System.currentTimeMillis());
		} catch (SQLException e) {
			throw new QueryException(e);
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
				DataAccess.log(start, sql, q.getParams());// 输出SQL执行日志
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return dt;
	}

	/**
	 * 分页查询，等同于executePagedDataTable(QueryBuilder qb, int pageSize, int pageIndex)
	 * 
	 * @param q 查询器
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页，以0为第一页
	 * @return 分页查询的结果
	 */
	public DataTable fetch(QueryBuilder q, int pageSize, int pageIndex) {
		return executePagedDataTable(q, pageSize, pageIndex);
	}

	/**
	 * 分页查询
	 * 
	 * @param q 查询器
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页，以0为第一页
	 * @return 分页查询的结果
	 */
	public DataTable executePagedDataTable(QueryBuilder q, int pageSize, int pageIndex) {
		long start = System.currentTimeMillis();
		prepareReadOnlyConn(null);
		if (pageSize < 1) {
			pageSize = 50;
		}
		if (pageIndex < 0) {
			pageIndex = 0;
		}
		q = getPagedQueryBuilder(readOnlyConn, q, pageSize, pageIndex);
		String pagedSQL = q.getSQL();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DataTable dt = null;
		try {
			boolean latin1Flag = readOnlyConn.getDBConfig().isLatin1Charset && readOnlyConn.getDBConfig().isOracle();
			if (latin1Flag) {
				try {
					pagedSQL = new String(pagedSQL.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			stmt = readOnlyConn.prepareStatement(pagedSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			setParams(stmt, q, readOnlyConn);
			rs = stmt.executeQuery();
			if (readOnlyConn.getDBConfig().isSQLServer2000()) {
				dt = new DataTable(readOnlyConn, rs, pageSize, pageIndex, latin1Flag);
			} else {
				dt = new DataTable(readOnlyConn, rs, latin1Flag);
				if (readOnlyConn.getDBConfig().isOracle() || readOnlyConn.getDBConfig().isDB2() || readOnlyConn.getDBConfig().isSQLServer()
						|| readOnlyConn.getDBConfig().isSybase()) {
					dt.deleteColumn(dt.getColumnCount() - 1);
				}
			}
			readOnlyConn.setLastSuccessExecuteTime(System.currentTimeMillis());
		} catch (SQLException e) {
			LogUtil.warn(pagedSQL);
			throw new QueryException(e);
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
				DataAccess.log(start, pagedSQL, q.getParams());// 输出SQL执行日志
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return dt;
	}

	/**
	 * 获取分页SQL
	 * 
	 * @param conn JDBC连接
	 * @param q 查询器
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页，以0为第一页
	 * @return 分页查询SQL
	 */
	public static QueryBuilder getPagedQueryBuilder(DBConn conn, QueryBuilder q, int pageSize, int pageIndex) {
		IDBType db = DBTypeService.getInstance().get(conn.getDBConfig().DBType);
		return db.getPagedQueryBuilder(conn, q, pageSize, pageIndex);
	}

	/**
	 * 通过针对查询器中的SQL构造一个对应的select count(*)语句来获知查询器执行结果的总条数
	 * 
	 * @param q 查询器
	 * @return 查询器执行结果的总条数。
	 */
	@SuppressWarnings("unchecked")
	public int getCount(QueryBuilder q) {
		Q cqb = new Q();
		cqb.setParams((ArrayList<Object>) q.getParams().clone());
		String sql = q.getSQL();
		int index1 = sql.lastIndexOf(")");
		int index2 = sql.toLowerCase().lastIndexOf("order by");
		if (index2 > index1) {
			sql = sql.substring(0, index2);
		}
		if (getConnection().getDBConfig().isMysql()) {
			SelectSQLParser ssp = new SelectSQLParser(sql);
			try {
				ssp.parse();
			} catch (Exception e) {
				throw new QueryException(e.getMessage());
			}
			cqb.setSQL(ssp.getMysqlCountSQL());
		} else {
			cqb.setSQL("select count(*) from (" + sql + ") t1");
		}
		DataTable dt = executeDataTable(cqb);
		return dt.getInt(0, 0);
	}

	/**
	 * 单值查询
	 * 
	 * @param q 查询器
	 * @return 返回查询器执行后的结果集的第一条记录中的第一个字段的值，如果没有记录则返回null
	 */
	public Object executeOneValue(QueryBuilder q) {
		long start = System.currentTimeMillis();
		prepareReadOnlyConn(q);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Object t = null;
		String sql = q.getSQL();
		try {
			boolean latin1Flag = readOnlyConn.getDBConfig().isLatin1Charset && readOnlyConn.getDBConfig().isOracle();
			if (latin1Flag) {
				try {
					sql = new String(sql.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			stmt = readOnlyConn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			setParams(stmt, q, readOnlyConn);
			rs = stmt.executeQuery();
			if (rs.next()) {
				t = rs.getObject(1);
				if (t instanceof Clob) {
					t = DBUtil.clobToString((Clob) t);
				}
				if (t instanceof Blob) {
					t = DBUtil.blobToBytes((Blob) t);
				}
			}
			readOnlyConn.setLastSuccessExecuteTime(System.currentTimeMillis());
		} catch (SQLException e) {
			throw new QueryException(e);
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
				DataAccess.log(start, sql, q.getParams());// 输出SQL执行日志
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return t;
	}

	/**
	 * 无返回值查询
	 * 
	 * @param q 查询器
	 * @return SQL影响到的记录条数
	 */
	public int executeNoQuery(QueryBuilder q) {
		long start = System.currentTimeMillis();
		if (conn == null) {
			conn = DBConnPoolManager.getConnection();
		}
		PreparedStatement stmt = null;
		int t = -1;
		String sql = q.getSQL();
		try {
			if (conn.getDBConfig().isLatin1Charset && conn.getDBConfig().isOracle()) {
				try {
					sql = new String(sql.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			setParams(stmt, q, conn);
			if (q.isBatchMode()) {
				stmt.executeBatch();
			} else {
				t = stmt.executeUpdate();
			}
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());
		} catch (SQLException e) {
			String lowerSQL = sql.trim().toLowerCase();
			if (lowerSQL.startsWith("delete ")) {
				throw new DeleteException(e);
			} else if (lowerSQL.startsWith("update ")) {
				throw new DeleteException(e);
			} else if (lowerSQL.startsWith("insert ")) {
				throw new InsertException(e);
			} else if (lowerSQL.startsWith("create ")) {
				throw new CreateException(e);
			} else if (lowerSQL.startsWith("drop ")) {
				throw new DropException(e);
			} else if (lowerSQL.startsWith("alter ")) {
				throw new AlterException(e);
			} else {
				throw new DatabaseException(e);
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				DataAccess.log(start, sql, q.getParams());// 输出SQL执行日志
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stmt = null;
		}
		return t;
	}

	/**
	 * 输出数据库操作相关日志
	 * 
	 * @param startTime 执行开始时间
	 * @param sql SQL语句
	 * @param params SQL变量
	 */
	public static void log(long startTime, String sql, List<Object> params) {
		String message = sql;
		long time = System.currentTimeMillis() - startTime;
		if (params != null && !sql.startsWith("Error:")) {
			Object[] arr = new String[params.size()];
			for (int i = 0; i < params.size(); i++) {
				Object obj = params.get(i);
				if (obj == null) {
					arr[i] = "null";
				} else if (obj instanceof String) {
					arr[i] = "'" + obj + "'";
				} else if (obj instanceof Date) {
					arr[i] = "'" + DateUtil.toDateTimeString((Date) obj) + "'";
				} else {
					arr[i] = obj.toString();
				}
			}
			message = StringFormat.format(sql, arr);
		}
		if (LogSQL.getValue()) {
			LogUtil.debug(time + "ms\t" + message);
		}
		// 扩展点,主要用于SQL日志分析
		ExtendManager.invoke(AfterSQLExecutedAction.ID, new Object[] { time, message });
	}

	/**
	 * 将DAO插入到数据库
	 * 
	 * @param dao　待插入数据库的DAO实例
	 */
	public void insert(DAO<?> dao) {
		dao.setDataAccess(this);
		dao.insert();
	}

	/**
	 * 更新DAO对应的数据库记录
	 * 
	 * @param dao 待更新到数据库的DAO实例
	 */
	public void update(DAO<?> dao) {
		dao.setDataAccess(this);
		dao.update();
	}

	/**
	 * 删除DAO对应的数据库记录
	 * 
	 * @param dao 待删除的DAO实例
	 */
	public void delete(DAO<?> dao) {
		dao.setDataAccess(this);
		dao.delete();
	}

	/**
	 * 备份DAO对应的数据库记录到B表，然后删除记录
	 * 
	 * @param dao 待删除的DAO实例
	 */
	public void deleteAndBackup(DAO<?> dao) {
		dao.setDataAccess(this);
		dao.deleteAndBackup();
	}

	/**
	 * 将DAO实例对应的数据记录删除，然后再插入。<br>
	 * 此方法是对于一些不太重要的数据的更新的简便写法
	 * 
	 * @param dao 待插入的DAO实例
	 */
	public void deleteAndInsert(DAO<?> dao) {
		dao.setDataAccess(this);
		dao.deleteAndInsert();
	}

	/**
	 * 将DAOSet中所有DAO插入到数据库
	 * 
	 * @param set 待插入的DAOSet实例
	 */
	public void insert(DAOSet<?> set) {
		set.setDataAccess(this);
		set.insert();
	}

	/**
	 * 更新DAOSet中所有DAO对应的数据库记录
	 * 
	 * @param set 待更新的DAOSet实例
	 */
	public void update(DAOSet<?> set) {
		set.setDataAccess(this);
		set.update();
	}

	/**
	 * 删除DAOSet中所有DAO对应的数据库记录
	 * 
	 * @param set 待删除的DAOSet实例
	 */
	public void delete(DAOSet<?> set) {
		set.setDataAccess(this);
		set.delete();
	}

	/**
	 * 备份DAOSet中所有DAO对应的数据库记录到B表，然后删除所有DAO对应的记录
	 * 
	 * @param set 待删除的DAOSet实例
	 */
	public void deleteAndBackup(DAOSet<?> set) {
		set.setDataAccess(this);
		set.deleteAndBackup();
	}

	/**
	 * 将DAOSet中所有DAO对应的数据记录删除，然后再插入。<br>
	 * 此方法是对于一些不太重要的数据的更新的简便写法
	 * 
	 * @param dao 待插入的DAOSet实例
	 */
	public void deleteAndInsert(DAOSet<?> set) {
		set.setDataAccess(this);
		set.deleteAndInsert();
	}

	/**
	 * 如果查询器中的SQL是select语句且读写分离启用则准备只读连接
	 * 
	 * @param q 查询器
	 */
	private void prepareReadOnlyConn(QueryBuilder q) {
		if (RWSpliting.getValue() && !rwsplitingDisabled) {
			if (conn == null || conn.getDBConfig().PoolName.equals(DBConnPoolManager.DEFAULT_POOLNAME + ".")) {
				if (q != null) {
					String sql = q.getSQL();
					if (DBUtil.isEndsWithForUpdate(DBConnPoolManager.getDBConnConfig(), sql)) {// for update语句必须放到写库里
						readOnlyConn = conn == null ? DBConnPoolManager.getConnection() : conn;
					} else {
						readOnlyConn = ReadOnlyDB.getReadOnlyDBConn(false);
					}
				}
			} else {
				readOnlyConn = conn;
			}
		} else {
			if (conn == null) {
				conn = DBConnPoolManager.getConnection();
			}
			readOnlyConn = conn;
		}
	}
}
