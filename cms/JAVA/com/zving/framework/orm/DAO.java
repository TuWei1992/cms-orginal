package com.zving.framework.orm;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.zving.framework.User;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.config.RWSpliting;
import com.zving.framework.config.ReadOnlyDB;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnPoolManager;
import com.zving.framework.data.DataAccess;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.QueryException;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.data.exception.DeleteException;
import com.zving.framework.data.exception.InsertException;
import com.zving.framework.data.exception.UpdateException;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * DAO虚拟类，表示数据表中的一条记录
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-8
 */
public abstract class DAO<T extends DAO<T>> {
	public static final String MEMO_DELETE = "Delete";

	protected boolean __outerConnFlag = false;// true为连接从外部传入，false为未传入连接

	protected boolean __operateColumnFlag = false;

	protected String __targetConnPool = null;// 操作数据库时使用的连接池名称

	protected int[] __operateColumnOrders;

	protected Object[] __oldValues;

	protected transient DataAccess __dataAccess;

	protected transient DAOMetadata __metadata;

	protected void _init() {
		if (__metadata == null) {
			__metadata = DAOMetadataManager.getMetadata(this.getClass());
		}
	}

	public DAOMetadata metadata() {
		_init();
		return __metadata;
	}

	public DAOColumn[] columns() {
		_init();
		return __metadata.getColumns();
	}

	public String table() {
		_init();
		return __metadata.getTable();
	}

	public boolean insert() {
		if (!__outerConnFlag) {
			__dataAccess = new DataAccess(DBConnPoolManager.getConnection(__targetConnPool));
		}
		_init();
		long start = System.currentTimeMillis();
		PreparedStatement pstmt = null;
		DBConn conn = __dataAccess.getConnection();
		IDBType db = DBTypeService.getInstance().get(__dataAccess.getConnection().getDBConfig().DBType);
		try {
			pstmt = conn.prepareStatement(__metadata.getInsertSQL(db), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ArrayList<Object> params = new ArrayList<Object>();
			int i = 0;
			for (DAOColumn sc : __metadata.getColumns()) {
				if (sc.isMandatory()) {
					if (__metadata.getV(this, sc.getColumnName()) == null) {
						String message = "Error:" + __metadata.getTable() + "'s column " + sc.getColumnName() + " cann't be null";
						// 输出SQL错误日志
						DataAccess.log(start, message, null);
						throw new SQLException(message);
					}
				}
				Object v = __metadata.getV(this, sc.getColumnName());
				params.add(v);
				DAOUtil.setParam(this, sc, pstmt, conn, i++, v);
			}
			int count = pstmt.executeUpdate();
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());
			if (count == 1) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			String message = "DAO insert to " + __metadata.getTable() + " failed:" + e.getMessage();
			LogUtil.warn(message);
			e.printStackTrace();
			InsertException e2 = new InsertException(message);
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			DataAccess.log(start, __metadata.getInsertSQL(db), null);// 输出SQL执行日志,不输出参数，以免日志中输出太多信息
			if (!__outerConnFlag) {
				__dataAccess.close();
			}
		}
	}

	public boolean isChanged(int order) {
		if (__oldValues == null || __oldValues.length <= order) {
			return true;
		} else {
			Object v1 = getFieldV(__metadata.getColumns()[order].getColumnName());
			Object v2 = __oldValues[order];
			if (v1 == null) {
				if (v2 == null) {
					return false;
				} else {
					return true;
				}
			} else {
				return !v1.equals(v2);
			}
		}
	}

	public boolean update() {
		// 检查是否满足update的条件，即主键是否已经置值
		_init();
		if (!__outerConnFlag) {
			__dataAccess = new DataAccess(DBConnPoolManager.getConnection(__targetConnPool));
		}
		long start = System.currentTimeMillis();
		DAOColumn[] columns = __metadata.getColumns();
		StringBuilder sb = new StringBuilder("update ");
		sb.append(__metadata.getTable());
		sb.append(" set ");
		boolean first = true;
		IDBType db = DBTypeService.getInstance().get(__dataAccess.getConnection().getDBConfig().DBType);
		if (__operateColumnFlag) {
			for (int i = 0; i < __operateColumnOrders.length; i++) {
				if (!isChanged(__operateColumnOrders[i])) {
					continue;
				}
				if (!first) {
					sb.append(",");
				} else {
					first = false;
				}
				String columnName = columns[__operateColumnOrders[i]].getColumnName();
				sb.append(db.maskColumnName(columnName));
				sb.append("=?");
			}
		} else {
			for (int i = 0; i < columns.length; i++) {
				if (!isChanged(i)) {
					continue;
				}
				if (!first) {
					sb.append(",");
				} else {
					first = false;
				}
				String columnName = columns[i].getColumnName();
				sb.append(db.maskColumnName(columnName));
				sb.append("=?");
			}
		}
		if (first) {// 没有需要修改的字段
			if (!__outerConnFlag) {
				__dataAccess.close();
			}
			return true;
		}
		sb.append(__metadata.getPrimaryKeyConditions(db));
		String sql = sb.toString();
		if (__dataAccess.getConnection().getDBConfig().isSybase()) {// todo: Sybase下的临时措施，将来要改掉
			sql = StringUtil.replaceAllIgnoreCase(sql, " Count=", "\"Count\"=");
			sql = StringUtil.replaceAllIgnoreCase(sql, " Scroll=", "\"Scroll\"=");
			sql = StringUtil.replaceAllIgnoreCase(sql, ",Count=", ",\"Count\"=");
			sql = StringUtil.replaceAllIgnoreCase(sql, ",Scroll=", ",\"Scroll\"=");
		}
		PreparedStatement pstmt = null;
		try {
			DBConn conn = __dataAccess.getConnection();
			ArrayList<Object> params = new ArrayList<Object>();
			pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			int i, j = 0;
			if (__operateColumnFlag) {
				for (i = 0; i < __operateColumnOrders.length; i++) {
					if (!isChanged(__operateColumnOrders[i])) {
						continue;
					}
					Object v = getV(__operateColumnOrders[i]);
					DAOUtil.setParam(this, columns[__operateColumnOrders[i]], pstmt, conn, j++, v);
					params.add(v);
				}
			} else {
				for (i = 0; i < columns.length; i++) {
					if (!isChanged(i)) {
						continue;
					}
					Object v = getV(i);
					DAOUtil.setParam(this, columns[i], pstmt, conn, j++, v);
					params.add(v);
				}
			}
			for (i = 0; i < columns.length; i++) {
				DAOColumn sc = columns[i];
				if (sc.isPrimaryKey()) {
					Object v = getV(i);
					if (__oldValues != null) {
						v = __oldValues[i];
					}
					if (v == null) {
						String message = __metadata.getTable() + "'s primary key column " + sc.getColumnName() + " can't be null";
						DataAccess.log(start, message, null);// 输出错误日志
						LogUtil.warn(message);
						return false;
					} else {
						if (__operateColumnFlag) {
							DAOUtil.setParam(this, columns[i], pstmt, conn, j++, v);
						} else {
							DAOUtil.setParam(this, columns[i], pstmt, conn, j++, v);
						}
						params.add(v);
					}
				}
			}
			pstmt.executeUpdate();
			refreshOldValues();
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());
			return true;
		} catch (Exception e) {
			String message = "DAO update to " + __metadata.getTable() + " failed:" + e.getMessage();
			DataAccess.log(start, sql, null);// 输出错误日志
			LogUtil.warn(message);
			UpdateException ue = new UpdateException(message);
			ue.setStackTrace(e.getStackTrace());
			throw ue;
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			DataAccess.log(start, sql, null);// 输出SQL执行日志
			if (!__outerConnFlag) {
				__dataAccess.close();
			}
		}
	}

	/**
	 * update之后更新旧的字段值，以便再次update时只更新上次update之后修改过的值
	 */
	void refreshOldValues() {
		if (__oldValues == null) {
			__oldValues = new Object[__metadata.getColumns().length];
		}
		for (int i = 0; i < __metadata.getColumns().length; i++) {
			__oldValues[i] = getFieldV(__metadata.getColumns()[i].getColumnName());
		}

	}

	public boolean delete() {
		delete2();
		return true;
	}

	/**
	 * @return 删除的记录条数
	 */
	public int delete2() {
		_init();
		if (!__outerConnFlag) {
			__dataAccess = new DataAccess(DBConnPoolManager.getConnection(__targetConnPool));
		}
		long start = System.currentTimeMillis();
		DAOColumn[] columns = __metadata.getColumns();
		PreparedStatement pstmt = null;
		ArrayList<Object> params = new ArrayList<Object>();
		DBConn conn = __dataAccess.getConnection();
		IDBType db = DBTypeService.getInstance().get(__dataAccess.getConnection().getDBConfig().DBType);
		try {
			pstmt = conn.prepareStatement(__metadata.getDeleteSQL(db), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			for (int i = 0, j = 0; i < columns.length; i++) {
				DAOColumn sc = columns[i];
				if (sc.isPrimaryKey()) {
					Object v = getV(i);
					if (__oldValues != null) {
						v = __oldValues[i];
					}
					if (v == null) {
						String message = "Error:" + __metadata.getTable() + "'s primary key column " + sc.getColumnName()
								+ " can't be null";
						DataAccess.log(start, message, null);
						LogUtil.warn(message);
						return 0;
					} else {
						DAOUtil.setParam(this, columns[i], pstmt, conn, j, v);
						params.add(v);
					}
					j++;
				}
			}
			int count = pstmt.executeUpdate();
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());
			return count;
		} catch (Exception e) {
			String message = "DAO delete from " + __metadata.getTable() + " failed:" + e.getMessage();
			DataAccess.log(start, message, null);
			LogUtil.warn(message);
			DeleteException e2 = new DeleteException(message);
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			DataAccess.log(start, __metadata.getDeleteSQL(db), params);// 输出SQL执行日志
			if (!__outerConnFlag) {
				__dataAccess.close();
			}
		}
	}

	public String backup() {
		return backup(null, null);
	}

	public String backup(String backupOperator, String backupMemo) {
		_init();
		if (!__outerConnFlag) {
			__dataAccess = new DataAccess(DBConnPoolManager.getConnection(__targetConnPool));
		}
		DBConn conn = __dataAccess.getConnection();
		IDBType db = DBTypeService.getInstance().get(__dataAccess.getConnection().getDBConfig().DBType);
		backupOperator = StringUtil.isEmpty(backupOperator) ? User.getUserName() : backupOperator;
		backupOperator = StringUtil.isEmpty(backupOperator) ? "SYSTEM" : backupOperator;
		@SuppressWarnings("unchecked")
		BackupDAO<T> bDao = new BackupDAO<T>((Class<T>) getClass());
		String backupSQL = bDao.getMetadata().getInsertSQL(db);
		long start = System.currentTimeMillis();
		DAOColumn[] columns = __metadata.getColumns();
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(backupSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ArrayList<Object> params = new ArrayList<Object>();
			int i = 0;
			for (; i < columns.length; i++) {
				DAOColumn sc = columns[i];
				if (sc.isMandatory()) {
					if (this.getV(i) == null) {
						String message = "Error:" + __metadata.getTable() + "'s column " + sc.getColumnName() + " can't be null";
						DataAccess.log(start, message, null);
						LogUtil.warn(message);
						throw new SQLException(message);
					}
				}
				Object v = getV(i);
				DAOUtil.setParam(this, sc, pstmt, conn, i, v);
				params.add(v);
			}
			String backupNo = DAOUtil.getBackupNo();
			DAOColumn sc = new DAOColumn("BackupNo", DataTypes.STRING, 15, 0, true, true);
			DAOUtil.setParam(this, sc, pstmt, conn, i, backupNo);
			params.add(backupNo);

			sc = new DAOColumn("BackupOperator", DataTypes.STRING, 200, 0, true, false);
			DAOUtil.setParam(this, sc, pstmt, conn, i + 1, backupOperator);
			params.add(backupOperator);

			sc = new DAOColumn("BackupTime", DataTypes.DATETIME, 0, 0, true, false);
			DAOUtil.setParam(this, sc, pstmt, conn, i + 2, new Date());
			params.add(new Date());

			sc = new DAOColumn("BackupMemo", DataTypes.STRING, 50, 0, false, false);
			DAOUtil.setParam(this, sc, pstmt, conn, i + 3, backupMemo);
			params.add(backupMemo);

			int count = pstmt.executeUpdate();
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());

			if (count == 1) {
				return backupNo;
			} else {
				return null;
			}
		} catch (Exception e) {
			String message = "DAO backup from " + __metadata.getTable() + " failed:" + e.getMessage();
			DataAccess.log(start, message, null);
			LogUtil.warn(message);
			DatabaseException e2 = new DatabaseException(message);
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			DataAccess.log(start, backupSQL, null);// 输出SQL执行日志
			if (!__outerConnFlag) {
				__dataAccess.close();
			}
		}
	}

	public boolean deleteAndInsert() {
		_init();
		if (__outerConnFlag) {
			if (!delete()) {
				return false;
			}
			return insert();
		} else {
			__dataAccess = new DataAccess(DBConnPoolManager.getConnection(__targetConnPool));
			__outerConnFlag = true;
			try {
				__dataAccess.setAutoCommit(false);
				delete();
				insert();
				__dataAccess.commit();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				__dataAccess.rollback();
				return false;
			} finally {
				__dataAccess.setAutoCommit(true);
				__dataAccess.close();
				this.__dataAccess = null;
				this.__outerConnFlag = false;
			}
		}
	}

	public boolean deleteAndBackup() {
		return deleteAndBackup(null, MEMO_DELETE);
	}

	public boolean deleteAndBackup(String backupOperator, String backupMemo) {
		_init();
		if (ObjectUtil.empty(backupMemo)) {
			backupMemo = MEMO_DELETE;
		}
		backupOperator = ObjectUtil.empty(backupOperator) ? User.getUserName() : backupOperator;
		backupOperator = ObjectUtil.empty(backupOperator) ? "SYSTEM" : backupOperator;
		if (!__outerConnFlag) {
			__dataAccess = new DataAccess(DBConnPoolManager.getConnection(__targetConnPool));
			__outerConnFlag = true;
			try {
				__dataAccess.setAutoCommit(false);
				backup(backupOperator, backupMemo);
				delete();
				__dataAccess.commit();
				return true;
			} catch (DatabaseException e) {
				LogUtil.warn("DAO deleteAndBackup from " + __metadata.getTable() + " failed:" + e.getMessage());
				e.printStackTrace();
				__dataAccess.rollback();
				return false;
			} finally {
				__outerConnFlag = false;
				try {
					__dataAccess.setAutoCommit(true);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				__dataAccess.close();
			}
		} else {

			if (backup(backupOperator, backupMemo) == null) {
				return false;
			}
			return delete();
		}
	}

	public boolean fill() {
		_init();
		if (!__outerConnFlag) {
			__dataAccess = new DataAccess(DBConnPoolManager.getConnection(__targetConnPool));
		}
		IDBType db = DBTypeService.getInstance().get(__dataAccess.getConnection().getDBConfig().DBType);
		// 检查是否满足fill的条件，即主键是否已经置值
		String sql = __metadata.getFillAllSQL(db);
		DAOColumn[] columns = __metadata.getColumns();
		if (__operateColumnFlag) {
			StringBuilder sb = new StringBuilder("select ");
			for (int i = 0; i < __operateColumnOrders.length; i++) {
				int order = __operateColumnOrders[i];
				if (i == 0) {
					sb.append(db.maskColumnName(columns[order].getColumnName()));
				} else {
					sb.append(",");
					sb.append(db.maskColumnName(columns[order].getColumnName()));
				}
			}
			sb.append(sql.substring(sql.indexOf(" from")));
			sql = sb.toString();
		}
		long start = System.currentTimeMillis();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ArrayList<Object> params = new ArrayList<Object>();
		try {
			DBConn conn = getReadOnlyDBConn(__dataAccess);
			pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			for (int i = 0, j = 0; i < columns.length; i++) {
				DAOColumn sc = columns[i];
				if (sc.isPrimaryKey()) {
					Object v = getV(i);
					if (v == null) {
						String message = "Error:" + __metadata.getTable() + "'s primary key column " + sc.getColumnName()
								+ " can't be null";
						DataAccess.log(start, message, null);
						throw new DAOException(message);
					} else {
						params.add(v);
						DAOUtil.setParam(this, columns[i], pstmt, conn, j, v);
					}
					j++;
				}
			}
			rs = pstmt.executeQuery();
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());

			if (rs.next()) {
				setVAll(conn, this, rs);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			String message = "DAO fill from " + __metadata.getTable() + " failed:" + e.getMessage();
			DataAccess.log(start, message, null);
			LogUtil.warn(message);
			QueryException e2 = new QueryException(message);
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			DataAccess.log(start, sql, params);// 输出SQL执行日志
			if (!__outerConnFlag) {
				__dataAccess.close();
			}
		}
	}

	public DAOSet<T> query(QueryBuilder wherePart, int pageSize, int pageIndex) {
		return fetch(wherePart, pageSize, pageIndex);
	}

	public DAOSet<T> query(QueryBuilder wherePart) {
		return fetch(wherePart);
	}

	public DAOSet<T> query() {
		return fetch(null);
	}

	public DAOSet<T> fetch() {
		return fetch(null);
	}

	public DAOSet<T> query(int pageSize, int pageIndex) {
		return fetch(null, pageSize, pageIndex);
	}

	public DAOSet<T> fetch(QueryBuilder wherePart) {
		return fetch(wherePart, -1, -1);
	}

	public DAOSet<T> fetch(QueryBuilder wherePart, int pageSize, int pageIndex) {
		_init();
		// 检查哪些字段有值
		long start = System.currentTimeMillis();
		DAOColumn[] columns = __metadata.getColumns();
		if (wherePart != null) {
			if (!wherePart.getSQL().trim().toLowerCase().startsWith("where")) {
				String message = "Error:QueryBuilder SQL must starts with where";
				DataAccess.log(start, message, null);
				throw new DAOException(message);
			}
		}
		if (!__outerConnFlag) {
			__dataAccess = new DataAccess(DBConnPoolManager.getConnection(__targetConnPool));
		}
		DBConn conn = getReadOnlyDBConn(__dataAccess);
		StringBuilder sb = new StringBuilder("select ");
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		QueryBuilder qb = new Q();// 不使用外面传入的Q，因为可能会多次复用
		String pageSQL = null;
		try {
			if (__operateColumnFlag) {
				for (int i = 0; i < __operateColumnOrders.length; i++) {
					if (i != 0) {
						sb.append(",");
					}
					sb.append(columns[__operateColumnOrders[i]].getColumnName());
				}
			} else {
				if (conn.getDBConfig().isSQLServer()) {// SQL
					// Server2005的分页必须有order by
					for (int i = 0; i < columns.length; i++) {
						if (i != 0) {
							sb.append(",");
						}
						sb.append(columns[i].getColumnName());
					}
				} else {
					sb.append("*");
				}
			}
			sb.append(" from " + __metadata.getTable());
			if (wherePart == null) {
				boolean firstFlag = true;
				for (int i = 0; i < columns.length; i++) {
					DAOColumn sc = columns[i];
					if (!isNull(sc)) {
						if (firstFlag) {
							sb.append(" where ");
							sb.append(sc.getColumnName());
							sb.append("=?");
							firstFlag = false;
						} else {
							sb.append(" and ");
							sb.append(sc.getColumnName());
							sb.append("=?");
						}
						Object v = getV(i);
						qb.add(v);
					}
				}
			} else {
				sb.append(" ");
				sb.append(wherePart.getSQL());
			}
			qb.setSQL(sb.toString());
			if (wherePart != null) {
				qb.getParams().addAll(wherePart.getParams());
			}

			pageIndex = pageIndex < 0 ? 0 : pageIndex;
			if (pageSize > 0) {
				qb = DataAccess.getPagedQueryBuilder(conn, qb, pageSize, pageIndex);
			}
			pageSQL = qb.getSQL();
			pstmt = conn.prepareStatement(pageSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			DataAccess.setParams(pstmt, qb, conn);
			rs = pstmt.executeQuery();

			conn.setLastSuccessExecuteTime(System.currentTimeMillis());
			DAOSet<T> set = new DAOSet<T>();
			while (rs.next()) {
				T dao = newInstance();
				if (this.__operateColumnFlag) {
					dao.__operateColumnFlag = true;
					dao.__operateColumnOrders = this.__operateColumnOrders;
				}
				setVAll(conn, dao, rs);
				set.add(dao);
			}
			return set;
		} catch (Exception e) {
			String message = "DAO query from " + __metadata.getTable() + " failed:" + e.getMessage();
			LogUtil.error(message);
			DataAccess.log(start, message, null);
			e.printStackTrace();
			return null;
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			DataAccess.log(start, qb.getSQL(), qb.getParams());// 输出执行的SQL
			if (!__outerConnFlag) {
				conn = null;
				__dataAccess.close();
			}
		}
	}

	private void setVAll(DBConn conn, DAO<?> dao, ResultSet rs) throws Exception {
		dao.__oldValues = new Object[dao.columnCount()];
		boolean latin1Flag = conn.getDBConfig().isLatin1Charset && conn.getDBConfig().isOracle();
		DAOColumn[] columns = __metadata.getColumns();
		IDBType db = DBTypeService.getInstance().get(conn.getDBConfig().DBType);
		if (__operateColumnFlag) {
			for (int order : __operateColumnOrders) {
				int index = rs.findColumn(columns[order].getColumnName());
				Object v = db.getValueFromResultSet(rs, index, columns[order].getColumnType(), latin1Flag);
				dao.setV(order, v);
				dao.__oldValues[order] = v;
			}
		} else {
			for (int i = 0; i < columns.length; i++) {
				int index = 0;
				try {
					index = rs.findColumn(columns[i].getColumnName());
				} catch (SQLException e) {
					throw new QueryException(e.getMessage() + ",Column=" + columns[i].getColumnName());
				}
				Object v = db.getValueFromResultSet(rs, index, columns[i].getColumnType(), latin1Flag);
				dao.setV(i, v);
				dao.__oldValues[i] = v;
			}
		}
	}

	public void setOperateColumns(String... colNames) {
		_init();
		if (colNames == null || colNames.length == 0) {
			__operateColumnFlag = false;
			return;
		}
		DAOColumn[] columns = __metadata.getColumns();
		__operateColumnOrders = new int[colNames.length];
		for (int i = 0, k = 0; i < colNames.length; i++) {
			boolean flag = false;
			for (int j = 0; j < columns.length; j++) {
				if (colNames[i].toString().equalsIgnoreCase(columns[j].getColumnName())) {
					__operateColumnOrders[k] = j;
					k++;
					flag = true;
					break;
				}
			}
			if (!flag) {
				throw new DAOException("Column not found in table " + __metadata.getTable() + ":" + colNames[i]);
			}
		}
		__operateColumnFlag = true;
	}

	public void setDataAccess(DataAccess dAccess) {
		__dataAccess = dAccess;
		__outerConnFlag = true;
	}

	private Object getFieldV(String columnName) {
		_init();
		try {
			for (Field f : __metadata.getDeclaredFields()) {
				if (f.getName().equalsIgnoreCase(columnName)) {
					return f.get(this);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected boolean isNull(DAOColumn sc) {
		_init();
		return getFieldV(sc.getColumnName()) == null;
	}

	/**
	 * 将map中的键值对按名称自动匹配到Schema的字段中去
	 */
	public void setValue(Map<?, ?> map) {
		setValue(map, null);
	}

	/**
	 * 将map中的key带有指定前缀的键值对按名称(去掉前缀后的key)自动匹配到Schema的字段中去，key不区分大小写
	 */
	public void setValue(Map<?, ?> map, String prefix) {// 主要用于从前台传值到Schema时自动区配
		_init();
		CaseIgnoreMapx<String, Object> cim = new CaseIgnoreMapx<String, Object>();
		for (Entry<?, ?> entry : map.entrySet()) {
			cim.put(entry.getKey() == null ? null : entry.getKey().toString(), entry.getValue());
		}
		DAOColumn[] columns = __metadata.getColumns();
		for (int j = 0; j < columns.length; j++) {
			DAOColumn sc = columns[j];
			String name = prefix == null ? sc.getColumnName() : prefix + sc.getColumnName();
			Object value = cim.get(name);
			if (value != null) {
				try {
					int type = sc.getColumnType();
					if (type == DataTypes.DATETIME) {
						if (value instanceof Date) {
							setV(j, value);
						} else if (value != null && !"".equals(value)) {
							if (DateUtil.isTime(value.toString())) {
								setV(j, DateUtil.parseDateTime(value.toString(), "HH:mm:ss"));
							} else {
								setV(j, DateUtil.parseDateTime(value.toString()));
							}
						}
					} else if (type == DataTypes.DOUBLE) {
						setV(j, new Double(value.toString()));
					} else if (type == DataTypes.FLOAT) {
						setV(j, new Float(value.toString()));
					} else if (type == DataTypes.LONG) {
						setV(j, new Long(value.toString()));
					} else if (type == DataTypes.INTEGER) {
						setV(j, new Integer(value.toString()));
					} else {
						setV(j, value);
					}
				} catch (Exception e) {
				}
			} else if (cim.containsKey(name)) {// 说明里面有值但是为null，这么写是为了提高性能
				setV(j, null);
			}
		}
	}

	/**
	 * 将Schema转化为DataRow
	 */
	public DataRow toDataRow() {
		_init();
		DAOColumn[] columns = __metadata.getColumns();
		int len = columns.length;
		DataColumn[] dcs = new DataColumn[len];
		Object[] values = new Object[len];
		for (int i = 0; i < len; i++) {
			DataColumn dc = new DataColumn();
			dc.setColumnName(columns[i].getColumnName());
			dc.setColumnType(columns[i].getColumnType());
			dcs[i] = dc;
			values[i] = getV(i);
		}
		DataTable dt = new DataTable(dcs, null);
		return new DataRow(dt, values);
	}

	/**
	 * 将DataRow中的字段值对按名称自动匹配到Schema的字段中去
	 */
	public void setValue(DataRow dr) {
		_init();
		DAOColumn[] columns = __metadata.getColumns();
		Object value = null;
		String key = null;
		for (int i = 0; i < dr.getColumnCount(); i++) {
			value = dr.get(i);
			key = dr.getDataColumns()[i].getColumnName();
			for (int j = 0; j < columns.length; j++) {
				DAOColumn sc = columns[j];
				if (key.equalsIgnoreCase(sc.getColumnName())) {
					try {
						int type = sc.getColumnType();
						if (type == DataTypes.DATETIME) {
							if (value instanceof Date) {
								setV(j, value);
							} else if (value != null && !"".equals(value)) {
								setV(j, DateUtil.parseDateTime(value.toString()));
							}
						} else if (type == DataTypes.DOUBLE) {
							setV(j, new Double(value.toString()));
						} else if (type == DataTypes.FLOAT) {
							setV(j, new Float(value.toString()));
						} else if (type == DataTypes.LONG) {
							setV(j, new Long(value.toString()));
						} else if (type == DataTypes.INTEGER) {
							setV(j, new Integer(value.toString()));
						} else {
							setV(j, value);
						}
						break;
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 返回修改前的旧数据
	 */
	public Object getOldValue(int order) {// NO_UCD
		if (__oldValues == null || __oldValues.length <= order) {
			return null;
		}
		return __oldValues[order];
	}

	/**
	 * 主要是调试时用，便于在Eclipse的调试界面直接看Schema里的内容
	 */
	@Override
	public String toString() {
		_init();
		DAOColumn[] columns = __metadata.getColumns();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0; i < columns.length; i++) {
			sb.append(columns[i].getColumnName());
			sb.append(":");
			sb.append(getV(i));
			sb.append(" ");
		}
		sb.append("}");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public T clone() {
		T s = this.newInstance();
		DAOUtil.copyFieldValue(this, s);
		return s;
	}

	/**
	 * 将Schema转化为Mapx
	 */
	public Mapx<String, Object> toMapx() {
		_init();
		DAOColumn[] columns = __metadata.getColumns();
		Mapx<String, Object> map = new Mapx<String, Object>();
		for (int i = 0; i < columns.length; i++) {
			map.put(columns[i].getColumnName(), getV(i));
		}
		return map;
	}

	/**
	 * 将Schmea转化小键名不区分大小写的Mapx
	 */
	public Mapx<String, Object> toCaseIgnoreMapx() {
		return new CaseIgnoreMapx<String, Object>(toMapx());
	}

	/**
	 * 获得Schema对应的数据表的字段数
	 */
	public int columnCount() {
		_init();
		return __metadata.getColumns().length;
	}

	/**
	 * 是否已经从持久层加载数据
	 */
	public boolean isLoaded() {
		return __oldValues != null;
	}

	/**
	 * 根据列名得到Schema中的列的描述
	 */
	public DAOColumn getColumn(String name) {
		_init();
		DAOColumn[] columns = __metadata.getColumns();
		for (DAOColumn sc : columns) {
			if (sc.getColumnName().equalsIgnoreCase(name)) {
				return sc;
			}
		}
		return null;
	}

	/**
	 * 设置名为columnName的字段的值
	 */
	public void setV(String columnName, Object v) {
		_init();
		__metadata.setV(this, columnName, v);
	}

	/**
	 * 将第i个字段的值设为v
	 */
	public void setV(int i, Object v) {
		_init();
		if (i < 0 || i >= __metadata.getColumns().length) {
			throw new DAOException("DAO.setV() failed,index out of range:" + __metadata.getTable());
		}
		DAOColumn c = __metadata.getColumns()[i];
		setV(c.getColumnName(), v);
	}

	/**
	 * 获取第i个字段的值
	 */
	public Object getV(int i) {
		_init();
		if (i < 0 || i >= __metadata.getColumns().length) {
			throw new DAOException("DAO.setV() failed,index out of range:" + __metadata.getTable());
		}
		DAOColumn c = __metadata.getColumns()[i];
		return getV(c.getColumnName());
	}

	/**
	 * 获取名为columnName的字段的值
	 */
	public Object getV(String columnName) {
		_init();
		return __metadata.getV(this, columnName);
	}

	@SuppressWarnings("unchecked")
	public T newInstance() {
		try {
			return (T) this.getClass().newInstance();
		} catch (Exception e) {
			throw new DAOException(e.getMessage());
		}
	}

	public DAOSet<T> newSet() {
		return new DAOSet<T>();
	}

	/**
	 * 返回索引信息，形如IndexName1:Column1,Column2;IndexName2:Column3<br>
	 * 即以分号隔开不同索引，冒号隔开索引名称和字段，字段之间以逗号隔开。
	 */
	public String indexInfo() {
		_init();
		return __metadata.getIndexes();
	}

	public String getTargetConnPool() {
		return __targetConnPool;
	}

	public void setTargetConnPool(String targetConnPool) {
		this.__targetConnPool = targetConnPool;
	}

	private DBConn getReadOnlyDBConn(DataAccess da) {
		DBConn conn = da.getConnection();
		DBConn readOnlyConn = conn;
		if (RWSpliting.getValue()) {
			if (conn == null || conn.getDBConfig().PoolName.equals(DBConnPoolManager.DEFAULT_POOLNAME + ".")) {
				readOnlyConn = ReadOnlyDB.getReadOnlyDBConn(false);
			}
		}
		return readOnlyConn;
	}
}
