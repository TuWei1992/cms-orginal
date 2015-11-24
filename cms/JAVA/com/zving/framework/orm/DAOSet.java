package com.zving.framework.orm;

import java.io.Serializable;
import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.zving.framework.User;
import com.zving.framework.collection.Filter;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnPoolManager;
import com.zving.framework.data.DataAccess;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.data.exception.DeleteException;
import com.zving.framework.data.exception.InsertException;
import com.zving.framework.data.exception.UpdateException;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * DAO集合，有序不可重复，对应于数据库中一组记录<br>
 * 作者：王育春<br>
 * 日期：2006-7-7<br>
 * 邮件：wyuch@zving.com<br>
 */
public class DAOSet<T extends DAO<T>> implements Serializable, Cloneable, Set<T> {
	private static final long serialVersionUID = 1L;

	protected static final int DefaultCapacity = 10;

	protected ArrayList<T> elementData;

	protected boolean outerConnFlag = false;// true为连接从外部传入，false为未传入连接

	protected boolean operateColumnFlag = false;

	protected String targetConnPool = null;// 操作数据库时使用的连接池名称

	protected int[] operateColumnOrders;

	protected transient DataAccess dataAccess;

	/**
	 * 初始化一个指定初始容量的DAOSet
	 */
	public DAOSet(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new RuntimeException("DAOSet's initialCapacity can't less than 0");
		}
		elementData = new ArrayList<T>(initialCapacity);
	}

	/**
	 * 初始化一个DAOSet，初始容量为10
	 */
	public DAOSet() {
		this(DefaultCapacity);
	}

	/**
	 * 为DAO设置DataAccess，设置DataAccess之后insert(),update()等操作都会使用此DataAccess进行
	 */
	public void setDataAccess(DataAccess dAccess) {
		dataAccess = dAccess;
		outerConnFlag = true;
	}

	/**
	 * 往Set中增加一个DAO
	 */
	@Override
	public boolean add(T s) {
		if (s == null) {
			return false;
		}
		if (!elementData.contains(s)) {
			elementData.add(s);
			return true;
		}
		return false;
	}

	/**
	 * 从Set中去掉一个DAO
	 */
	public boolean remove(T t) {
		if (t == null) {
			return false;
		}
		return elementData.remove(t);
	}

	/**
	 * 清除掉Set中的所有DAO
	 */
	@Override
	public void clear() {
		elementData.clear();
	}

	/**
	 * 判断当前Set中是否有DAO
	 */
	@Override
	public boolean isEmpty() {
		return elementData.size() == 0;
	}

	/**
	 * 返回第index个Schmea
	 */
	public T get(int index) {
		return elementData.get(index);
	}

	/**
	 * 设置第index个DAO
	 */
	public boolean set(int index, T t) {// NO_UCD
		elementData.set(index, t);
		return true;
	}

	/**
	 * 返回DAOSet中DAO的个数
	 */
	@Override
	public int size() {
		return elementData.size();
	}

	/**
	 * 将DAOSet中的所有数据插入到数据库中
	 */
	public boolean insert() {
		if (size() == 0) {
			return true;
		}
		DAOMetadata meta = get(0).metadata();
		if (!outerConnFlag) {
			dataAccess = new DataAccess(DBConnPoolManager.getConnection(targetConnPool));
		}
		long start = System.currentTimeMillis();
		PreparedStatement pstmt = null;
		DBConn conn = dataAccess.getConnection();
		IDBType db = DBTypeService.getInstance().get(dataAccess.getConnection().getDBConfig().DBType);
		try {
			boolean autoComit = conn.getAutoCommit();
			if (!outerConnFlag) {
				conn.setAutoCommit(false);
			}
			pstmt = conn.prepareStatement(meta.getInsertSQL(db), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			for (T dao : elementData) {
				int i = 0;
				for (DAOColumn sc : meta.getColumns()) {
					if (sc.isMandatory()) {
						if (dao.getV(i) == null) {
							LogUtil.warn(meta.getTable() + "'s mandatory column " + sc.getColumnName() + " can't be null");
							return false;
						}
					}
					Object v = dao.getV(i);
					DAOUtil.setParam(dao, sc, pstmt, conn, i++, v);
				}
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			if (!outerConnFlag) {
				conn.commit();
				conn.setAutoCommit(autoComit);
			}
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());
			return true;
		} catch (Throwable e) {
			// 批量增加时发生异常很难调试，例如字段超长，主键冲突等，需要输出整个DAOSet到日志中
			if (e instanceof BatchUpdateException) {
				LogUtil.warn(this.toDataTable());
			}
			String message = "Error:Set insert to " + meta.getTable() + " failed:" + e.getMessage();
			LogUtil.warn(message);
			DataAccess.log(start, message, null);// 输出SQL日志
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
			DataAccess.log(start, meta.getInsertSQL(db), null);// 输出SQL日志
			if (!outerConnFlag) {
				dataAccess.close();
			}
		}
	}

	/**
	 * 将DAOSet中的所有数据更新到数据库
	 */
	public boolean update() {
		if (size() == 0) {
			return true;
		}
		DAOMetadata meta = get(0).metadata();
		String sql = null;
		if (!outerConnFlag) {
			dataAccess = new DataAccess(DBConnPoolManager.getConnection(targetConnPool));
		}
		IDBType db = DBTypeService.getInstance().get(dataAccess.getConnection().getDBConfig().DBType);
		long start = System.currentTimeMillis();
		if (size() == 1) {
			elementData.get(0).setDataAccess(dataAccess);
			elementData.get(0).update();// 如果只有一个，则调用DAO.update()更新修改过的字段，
		}
		if (operateColumnFlag) {
			StringBuilder sb = new StringBuilder("update ");
			sb.append(meta.getTable());
			sb.append(" set ");
			for (int i = 0; i < operateColumnOrders.length; i++) {
				if (i != 0) {
					sb.append(",");
				}
				String column = meta.getColumns()[operateColumnOrders[i]].getColumnName();
				sb.append(db.maskColumnName(column));
				sb.append("=?");
			}
			sb.append(meta.getPrimaryKeyConditions(db));
			sql = sb.toString();
		} else {
			StringBuilder sb = new StringBuilder("update ");
			sb.append(meta.getTable());
			sb.append(" set ");
			for (int i = 0; i < meta.getColumns().length; i++) {
				if (i != 0) {
					sb.append(",");
				}
				String column = meta.getColumns()[i].getColumnName();
				sb.append(db.maskColumnName(column));
				sb.append("=?");
			}
			sb.append(meta.getPrimaryKeyConditions(db));
			sql = sb.toString();
		}
		if (dataAccess.getConnection().getDBConfig().isSybase()) {// Sybase下的临时措施，将来要改掉
			sql = StringUtil.replaceAllIgnoreCase(sql, " Count=", "\"Count\"=");
			sql = StringUtil.replaceAllIgnoreCase(sql, " Scroll=", "\"Scroll\"=");
			sql = StringUtil.replaceAllIgnoreCase(sql, ",Count=", ",\"Count\"=");
			sql = StringUtil.replaceAllIgnoreCase(sql, ",Scroll=", ",\"Scroll\"=");
		}
		PreparedStatement pstmt = null;
		try {
			DBConn conn = dataAccess.getConnection();
			boolean autoComit = conn.getAutoCommit();
			if (!outerConnFlag) {
				conn.setAutoCommit(false);
			}
			pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			for (T dao : elementData) {
				if (operateColumnFlag) {
					for (int i = 0; i < operateColumnOrders.length; i++) {
						Object v = dao.getV(operateColumnOrders[i]);
						DAOUtil.setParam(dao, meta.getColumns()[operateColumnOrders[i]], pstmt, conn, i, v);
					}
				} else {
					for (int i = 0; i < meta.getColumns().length; i++) {
						Object v = dao.getV(i);
						DAOUtil.setParam(dao, meta.getColumns()[i], pstmt, conn, i, v);
					}
				}
				for (int i = 0, j = 0; i < meta.getColumns().length; i++) {
					DAOColumn sc = meta.getColumns()[i];
					if (sc.isPrimaryKey()) {
						Object v = dao.getV(i);
						if (dao.__oldValues != null) {
							v = dao.__oldValues[i];
						}
						if (v == null) {
							LogUtil.warn(meta.getTable() + "'s primary column " + sc.getColumnName() + " can't be null");
							return false;
						} else {
							if (operateColumnFlag) {
								DAOUtil.setParam(dao, meta.getColumns()[i], pstmt, conn, j + operateColumnOrders.length, v);
							} else {
								DAOUtil.setParam(dao, meta.getColumns()[i], pstmt, conn, j + meta.getColumns().length, v);
							}
						}
						j++;
					}
				}
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			if (!outerConnFlag) {
				conn.commit();
				conn.setAutoCommit(autoComit);
			}
			for (T dao : elementData) {
				dao.refreshOldValues();
			}
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());
			return true;
		} catch (Throwable e) {
			String message = "Error:Set update to " + meta.getTable() + " failed:" + e.getMessage();
			DataAccess.log(start, message, null);// 输出SQL日志
			LogUtil.warn(message);
			UpdateException e2 = new UpdateException(message);
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
			DataAccess.log(start, sql, null);// 输出SQL日志
			if (!outerConnFlag) {
				dataAccess.close();
			}
		}
	}

	/**
	 * 按主键删除DAOSet中的所有记录
	 */
	public boolean delete() {
		if (size() == 0) {
			return true;
		}
		DAOMetadata meta = get(0).metadata();
		if (!outerConnFlag) {
			dataAccess = new DataAccess(DBConnPoolManager.getConnection(targetConnPool));
		}
		long start = System.currentTimeMillis();
		PreparedStatement pstmt = null;
		DBConn conn = dataAccess.getConnection();
		IDBType db = DBTypeService.getInstance().get(dataAccess.getConnection().getDBConfig().DBType);
		try {
			boolean autoComit = conn.getAutoCommit();
			if (!outerConnFlag) {
				conn.setAutoCommit(false);
			}
			pstmt = conn.prepareStatement(meta.getDeleteSQL(db), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			for (T dao : elementData) {
				for (int i = 0, j = 0; i < meta.getColumns().length; i++) {
					DAOColumn sc = meta.getColumns()[i];
					if (sc.isPrimaryKey()) {
						Object v = dao.getV(i);
						if (dao.__oldValues != null) {
							v = dao.__oldValues[i];
						}
						if (v == null) {
							LogUtil.warn(meta.getTable() + "'s primary column " + sc.getColumnName() + " can't be null");
							return false;
						} else {
							DAOUtil.setParam(dao, meta.getColumns()[i], pstmt, conn, j, v);
						}
						j++;
					}
				}
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			if (!outerConnFlag) {
				conn.commit();
				conn.setAutoCommit(autoComit);
			}
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());
			return true;
		} catch (Throwable e) {
			String message = "Error:Set delete from " + meta.getTable() + " failed:" + e.getMessage();
			LogUtil.warn(message);
			DataAccess.log(start, message, null);// 输出SQL日志
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
			DataAccess.log(start, meta.getDeleteSQL(db), null);// 输出SQL日志
			if (!outerConnFlag) {
				dataAccess.close();
			}
		}
	}

	/**
	 * 按主键删除Set中的所有数据后再插入，某些情况下为保证插入不会失败而使用本方法
	 */
	public boolean deleteAndInsert() {
		if (size() == 0) {
			return true;
		}
		if (outerConnFlag) {
			if (!delete()) {
				return false;
			}
			return insert();
		} else {
			dataAccess = new DataAccess(DBConnPoolManager.getConnection(targetConnPool));
			outerConnFlag = true;
			try {
				dataAccess.setAutoCommit(false);
				delete();
				insert();
				dataAccess.commit();
				return true;
			} catch (Throwable e) {
				e.printStackTrace();
				dataAccess.rollback();
				return false;
			} finally {
				try {
					dataAccess.setAutoCommit(true);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				dataAccess.close();
				this.dataAccess = null;
				this.outerConnFlag = false;
			}
		}
	}

	/**
	 * 删除Set中的所有记录并备份到B表
	 */
	public boolean deleteAndBackup() {
		return deleteAndBackup(null, DAO.MEMO_DELETE);
	}

	/**
	 * 删除Set中的所有记录并备份到B表，并指定备份人和备份备注
	 */
	public boolean deleteAndBackup(String backupOperator, String backupMemo) {
		if (size() == 0) {
			return true;
		}
		if (ObjectUtil.empty(backupMemo)) {
			backupMemo = DAO.MEMO_DELETE;
		}
		try {
			backupOperator = ObjectUtil.empty(backupOperator) ? User.getUserName() : backupOperator;
			backupOperator = ObjectUtil.empty(backupOperator) ? "SYSTEM" : backupOperator;
			if (outerConnFlag) {
				if (!backup(backupOperator, backupMemo)) {
					return true;
				}
				delete();
				return true;
			} else {
				dataAccess = new DataAccess(DBConnPoolManager.getConnection(targetConnPool));
				outerConnFlag = true;
				try {
					dataAccess.setAutoCommit(false);
					backup(backupOperator, backupMemo);
					delete();
					dataAccess.commit();
					return true;
				} catch (Throwable e) {
					e.printStackTrace();
					dataAccess.rollback();
					return false;
				} finally {
					try {
						dataAccess.setAutoCommit(true);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					dataAccess.close();
					this.dataAccess = null;
					this.outerConnFlag = false;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将Set中的所有记录备份到B表，并指定备份人和备份备注
	 */
	public boolean backup(String backupOperator, String backupMemo) {
		if (size() == 0) {
			return true;
		}
		DAOMetadata meta = get(0).metadata();
		backupOperator = StringUtil.isEmpty(backupOperator) ? User.getUserName() : backupOperator;
		backupOperator = StringUtil.isEmpty(backupOperator) ? "SYSTEM" : backupOperator;
		@SuppressWarnings("unchecked")
		BackupDAO<T> bDao = new BackupDAO<T>((Class<T>) get(0).getClass());
		Date current = new Date();
		if (!outerConnFlag) {
			dataAccess = new DataAccess(DBConnPoolManager.getConnection(targetConnPool));
		}
		DBConn conn = dataAccess.getConnection();
		IDBType db = DBTypeService.getInstance().get(dataAccess.getConnection().getDBConfig().DBType);
		String backupSQL = bDao.getMetadata().getInsertSQL(db);

		long start = System.currentTimeMillis();
		PreparedStatement pstmt = null;
		try {
			boolean autoComit = conn.getAutoCommit();
			if (!outerConnFlag) {
				conn.setAutoCommit(false);
			}
			int i = meta.getColumns().length;
			DAOColumn sc1 = new DAOColumn("BackupNo", DataTypes.STRING, 15, 0, true, true);
			DAOColumn sc2 = new DAOColumn("BackupOperator", DataTypes.STRING, 200, 0, true, false);
			DAOColumn sc3 = new DAOColumn("BackupTime", DataTypes.DATETIME, 0, 0, true, false);
			DAOColumn sc4 = new DAOColumn("BackupMemo", DataTypes.STRING, 50, 0, false, false);
			pstmt = conn.prepareStatement(backupSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			for (T dao : elementData) {
				int j = 0;
				for (DAOColumn sc : meta.getColumns()) {
					if (sc.isMandatory()) {
						if (dao.getV(j) == null) {
							LogUtil.warn(meta.getTable() + "'s mandatory column " + sc.getColumnName() + " can't be null");
							return false;
						}
					}
					Object v = dao.getV(j);
					DAOUtil.setParam(dao, sc, pstmt, conn, j++, v);
				}
				DAOUtil.setParam(dao, sc1, pstmt, conn, i, DAOUtil.getBackupNo());
				DAOUtil.setParam(dao, sc2, pstmt, conn, i + 1, backupOperator);
				DAOUtil.setParam(dao, sc3, pstmt, conn, i + 2, current);
				DAOUtil.setParam(dao, sc4, pstmt, conn, i + 3, backupMemo);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			if (!outerConnFlag) {
				conn.commit();
				conn.setAutoCommit(autoComit);
			}
			conn.setLastSuccessExecuteTime(System.currentTimeMillis());

			return true;
		} catch (Throwable e) {
			// 批量增加时发生异常很难调试，例如字段超长，主键冲突等，需要输出整个DAOSet到日志中
			if (e instanceof BatchUpdateException) {
				LogUtil.warn(this.toDataTable());
			}
			String message = "Error:Set backup from " + meta.getTable() + " failed:" + e.getMessage();
			LogUtil.warn(message);
			e.printStackTrace();
			DataAccess.log(start, message, null);// 输出SQL日志
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
			DataAccess.log(start, backupSQL, null);// 输出SQL日志
			if (!outerConnFlag) {
				dataAccess.close();
			}
		}
	}

	private void dealOperateColumnNames(String[] operateColumnNames) {
		if (size() == 0) {
			return;
		}
		DAOMetadata meta = get(0).metadata();
		if (operateColumnNames != null) {
			operateColumnOrders = new int[operateColumnNames.length];
			for (int i = 0, k = 0; i < operateColumnNames.length; i++) {
				boolean flag = false;
				for (int j = 0; j < meta.getColumns().length; j++) {
					if (operateColumnNames[i].toString().toLowerCase().equals(meta.getColumns()[j].getColumnName().toLowerCase())) {
						operateColumnOrders[k] = j;
						k++;
						flag = true;
						break;
					}
				}
				if (!flag) {
					throw new RuntimeException("Column not found:" + operateColumnNames[i]);
				}
			}
		}
	}

	/**
	 * 按字段名指定要操作的列，调用本方法后，query()时只会获取指定,update()时只会更新指定列
	 */
	public void setOperateColumns(String... colNames) {
		if (colNames == null || colNames.length == 0) {
			operateColumnFlag = false;
			return;
		}
		dealOperateColumnNames(colNames);
		operateColumnFlag = true;
	}

	/**
	 * 将DAOSet转化为DataTable
	 */
	public DataTable toDataTable() {
		if (size() == 0) {
			return new DataTable();
		}
		DAOMetadata meta = get(0).metadata();
		if (operateColumnFlag) {
			DataColumn[] dcs = new DataColumn[operateColumnOrders.length];
			Object[][] values = new Object[size()][meta.getColumns().length];
			for (int i = 0; i < operateColumnOrders.length; i++) {
				DataColumn dc = new DataColumn();
				dc.setColumnName(meta.getColumns()[operateColumnOrders[i]].getColumnName());
				dc.setColumnType(meta.getColumns()[operateColumnOrders[i]].getColumnType());
				dcs[i] = dc;
			}
			for (int i = 0; i < size(); i++) {
				for (int j = 0; j < operateColumnOrders.length; j++) {
					values[i][j] = elementData.get(i).getV(operateColumnOrders[j]);
				}
			}
			DataTable dt = new DataTable(dcs, values);
			return dt;
		}
		DataColumn[] dcs = new DataColumn[meta.getColumns().length];
		Object[][] values = new Object[size()][meta.getColumns().length];
		for (int i = 0; i < meta.getColumns().length; i++) {
			DataColumn dc = new DataColumn();
			dc.setColumnName(meta.getColumns()[i].getColumnName());
			dc.setColumnType(meta.getColumns()[i].getColumnType());
			dcs[i] = dc;
		}
		for (int i = 0; i < size(); i++) {
			for (int j = 0; j < meta.getColumns().length; j++) {
				values[i][j] = elementData.get(i).getV(j);
			}
		}
		DataTable dt = new DataTable(dcs, values);
		return dt;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DAOSet<T> clone() {
		DAOSet<T> set = newSet();
		for (int i = 0; i < this.size(); i++) {
			set.add(elementData.get(i).clone());
		}
		return set;
	}

	/**
	 * 按指定的比较器类排序
	 */
	public void sort(Comparator<T> c) {
		elementData = (ArrayList<T>) ObjectUtil.sort(elementData, c);
	}

	/**
	 * 按指定的列逆序排列
	 */
	public void sort(String columnName) {// NO_UCD
		sort(columnName, "desc", false);
	}

	/**
	 * 按指定的列、排序方式（desc或asc）排序
	 */
	public void sort(String columnName, String order) {
		sort(columnName, order, false);
	}

	/**
	 * 按指定的列、排序方式（desc或asc）排序，如果isNumber为空，则比较大小时先将字段值转化为数值
	 */
	public void sort(final String columnName, final String order, final boolean isNumber) {
		sort(new Comparator<T>() {
			@Override
			public int compare(T obj1, T obj2) {
				Object v1 = obj1.getV(columnName);
				Object v2 = obj2.getV(columnName);
				if (v1 == null) {
					if (v2 != null) {
						return -1;
					} else {
						return 0;
					}
				} else if (v2 == null) {
					return 1;
				}
				if (v1 instanceof Number && v2 instanceof Number) {
					double d1 = ((Number) v1).doubleValue();
					double d2 = ((Number) v2).doubleValue();
					if (d1 == d2) {
						return 0;
					} else if (d1 > d2) {
						return "asc".equalsIgnoreCase(order) ? 1 : -1;
					} else {
						return "asc".equalsIgnoreCase(order) ? -1 : 1;
					}
				} else if (v1 instanceof Date && v2 instanceof Date) {
					Date d1 = (Date) v1;
					Date d2 = (Date) v2;
					if ("asc".equalsIgnoreCase(order)) {
						return d1.compareTo(d2);
					} else {
						return -d1.compareTo(d2);
					}
				} else if (isNumber) {
					double d1 = 0, d2 = 0;
					try {
						d1 = Double.parseDouble(String.valueOf(v1));
						d2 = Double.parseDouble(String.valueOf(v2));
					} catch (Exception e) {
					}
					if (d1 == d2) {
						return 0;
					} else if (d1 > d2) {
						return "asc".equalsIgnoreCase(order) ? -1 : 1;
					} else {
						return "asc".equalsIgnoreCase(order) ? 1 : -1;
					}
				} else {
					int c = v1.toString().compareTo(v2.toString());
					if ("asc".equalsIgnoreCase(order)) {
						return c;
					} else {
						return -c;
					}
				}
			}
		});
	}

	/**
	 * 使用指定的过滤器过滤掉DAOSet中的DAO
	 */
	public DAOSet<T> filter(Filter<T> filter) {// NO_UCD
		DAOSet<T> set = newSet();
		for (T t : elementData) {
			if (filter.filter(t)) {
				set.add(t.clone());
			}
		}
		return this;
	}

	public DAOSet<T> newSet() {
		return new DAOSet<T>();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (T t : elementData) {
			sb.append(t + "\n");
		}
		return sb.toString();
	}

	@Override
	public Iterator<T> iterator() {
		return elementData.iterator();
	}

	public String getTargetConnPool() {
		return targetConnPool;
	}

	public void setTargetConnPool(String targetConnPool) {
		this.targetConnPool = targetConnPool;
	}

	@Override
	public boolean contains(Object o) {
		return elementData.contains(o);
	}

	@Override
	public Object[] toArray() {
		return elementData.toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {// NO_UCD
		return elementData.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return elementData.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return elementData.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return elementData.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return elementData.removeAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return elementData.removeAll(c);
	}
}
