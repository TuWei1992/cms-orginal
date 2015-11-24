package com.zving.framework.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.exception.DatabaseException;

/**
 * SQL查询器，用于构造参数化SQL并执行，以避免SQL注入。支持批量模式。
 * 
 * @Author 王育春
 * @Date 2008-7-29
 * @Mail wyuch@zving.com
 */
public class QueryBuilder {
	ArrayList<Object> params = new ArrayList<Object>();

	ArrayList<ArrayList<Object>> batches;

	String targetConnPool = null;// 操作数据库时使用的连接池名称

	private boolean batchMode;

	StringBuilder sql = new StringBuilder();

	/**
	 * 批量操作时，返回所有批量操作的参数列表
	 */
	public ArrayList<ArrayList<Object>> getBatches() {
		return batches;
	}

	/**
	 * 构造一个空的查询，等待使用setSQL()方法设置SQL语句
	 */
	public QueryBuilder() {
	}

	/**
	 * 根据传入的SQL字符串构造一个SQL查询，参数个数可变
	 */
	public QueryBuilder(String sql, Object... args) {
		setSQL(sql);
		if (args != null) {
			for (Object obj : args) {
				add(obj);
			}
		}
	}

	/**
	 * 当前SQL操作是否是批量模式
	 */
	public boolean isBatchMode() {
		return batchMode;
	}

	/**
	 * 设置批量模式
	 */
	public void setBatchMode(boolean batchMode) {
		if (batchMode && batches == null) {
			batches = new ArrayList<ArrayList<Object>>();
		}
		this.batchMode = batchMode;
	}

	/**
	 * 增加一个批次
	 */
	public void addBatch() {
		if (!batchMode) {
			throw new RuntimeException("Must invoke setBatchMode(true) before addBatch()");
		}
		batches.add(params);
		params = new ArrayList<Object>();
	}

	/**
	 * 添加SQL参数值
	 */
	public QueryBuilder add(Object... params) {
		for (Object param : params) {
			this.params.add(param);
		}
		return this;
	}

	/**
	 * 添加SQL参数值
	 */
	public QueryBuilder add(Collection<?> params) {
		for (Object param : params) {
			this.params.add(param);
		}
		return this;
	}

	/**
	 * 设置指定位置的SQL参数
	 */
	public QueryBuilder set(int index, Object param) {// NO_UCD
		params.set(index, param);
		return this;
	}

	/**
	 * 设置SQL语句
	 */
	public QueryBuilder setSQL(String sql) {
		this.sql = new StringBuilder(sql);
		return this;
	}

	/**
	 * 追加部分SQL语句，同时追加SQL参数
	 */
	public QueryBuilder append(String sqlPart, Object... params) {
		sql.append(sqlPart);
		if (params == null) {
			return this;
		}
		for (Object obj : params) {
			add(obj);
		}
		return this;
	}

	/**
	 * 执行查询，返回DataTable
	 */
	public DataTable executeDataTable() {
		return executeDataTable(targetConnPool);
	}

	/**
	 * 在执定连接池上执行查询，返回DataTable
	 */
	public DataTable executeDataTable(String poolName) {
		DataAccess da = new DataAccess(DBConnPoolManager.getConnection(poolName));
		DataTable dt = null;
		try {
			dt = da.executeDataTable(this);
		} catch (Throwable e) {
			DataAccess.log(System.currentTimeMillis(), "Error:" + e.getMessage(), null);
			if (e instanceof QueryException) {
				throw (QueryException) e;
			}
			throw new QueryException(e);
		} finally {
			da.close();
		}
		return dt;
	}

	/**
	 * 以分页方式执行查询，并返回代表指定页的DataTable
	 */
	public DataTable executePagedDataTable(int pageSize, int pageIndex) {
		return executePagedDataTable(targetConnPool, pageSize, pageIndex);
	}

	public DataTable executePagedDataTable(String poolName, int pageSize, int pageIndex) {
		DataAccess da = new DataAccess(DBConnPoolManager.getConnection(poolName));
		DataTable dt = null;
		try {
			dt = da.executePagedDataTable(this, pageSize, pageIndex);
		} catch (Throwable e) {
			DataAccess.log(System.currentTimeMillis(), "Error:" + e.getMessage(), null);
			if (e instanceof QueryException) {
				throw (QueryException) e;
			}
			throw new QueryException(e);
		} finally {
			da.close();
		}
		return dt;
	}

	/**
	 * 执行查询，并返回第一条记录的第一个字段的值，如果没有记录，则返回null
	 */
	public Object executeOneValue() {
		return executeOneValue(targetConnPool);
	}

	/**
	 * 在指定连接池上执行查询，并返回第一条记录的第一个字段的值，如果没有记录，则返回null
	 */
	public Object executeOneValue(String poolName) {
		DataAccess da = new DataAccess(DBConnPoolManager.getConnection(poolName));
		Object t = null;
		try {
			t = da.executeOneValue(this);
		} catch (Throwable e) {
			if (e instanceof QueryException) {
				throw (QueryException) e;
			}
			throw new QueryException(e);
		} finally {
			da.close();
		}
		return t;
	}

	/**
	 * 执行查询，并返回第一条记录的第一个字段的值并转化为String，如果没有记录，则返回null
	 */
	public String executeString() {
		return executeString(targetConnPool);
	}

	/**
	 * 在指定连接池上执行查询，并返回第一条记录的第一个字段的值并转化为String，如果没有记录，则返回null
	 */
	public String executeString(String poolName) {
		Object o = executeOneValue(poolName);
		if (o == null) {
			return null;
		} else {
			return o.toString();
		}
	}

	/**
	 * 执行查询，并返回第一条记录的第一个字段的值并转化为int
	 */
	public int executeInt() {
		return executeInt(targetConnPool);
	}

	/**
	 * 在指定连接池上执行查询，并返回第一条记录的第一个字段的值并转化为int
	 */
	public int executeInt(String poolName) {
		Object o = executeOneValue(poolName);
		if (o == null) {
			return 0;
		} else {
			if (o instanceof Number) {
				return ((Number) o).intValue();
			} else {
				return Integer.parseInt(o.toString());
			}
		}
	}

	/**
	 * 执行查询，并返回第一条记录的第一个字段的值并转化为long
	 */
	public long executeLong() {
		return executeLong(targetConnPool);
	}

	/**
	 * 在指定连接池上执行查询，并返回第一条记录的第一个字段的值并转化为long
	 */
	public long executeLong(String poolName) {
		Object o = executeOneValue(poolName);
		if (o == null) {
			return 0;
		} else {
			if (o instanceof Number) {
				return ((Number) o).longValue();
			} else {
				return Long.parseLong(o.toString());
			}
		}
	}

	/**
	 * 执行查询，并返回第一条记录的第一个字段的值并转化为double
	 */
	public Double executeDouble() {// NO_UCD
		return executeDouble(targetConnPool);
	}

	/**
	 * 在指定连接池上执行查询，并返回第一条记录的第一个字段的值并转化为double
	 */
	public Double executeDouble(String poolName) {
		Object o = executeOneValue(poolName);
		if (o == null) {
			return 0d;
		} else {
			if (o instanceof Number) {
				return ((Number) o).doubleValue();
			} else {
				return Double.parseDouble(o.toString());
			}
		}
	}

	/**
	 * 执行查询，并返回第一条记录的第一个字段的值并转化为float
	 */
	public Float executeFloat() {// NO_UCD
		return executeFloat(targetConnPool);
	}

	/**
	 * 在指定连接池上执行查询，并返回第一条记录的第一个字段的值并转化为float
	 */
	public Float executeFloat(String poolName) {
		Object o = executeOneValue(poolName);
		if (o == null) {
			return 0f;
		} else {
			if (o instanceof Number) {
				return ((Number) o).floatValue();
			} else {
				return Float.parseFloat(o.toString());
			}
		}
	}

	/**
	 * 执行查询，并返回查询影响的记录数。如果发生SQL异常，则抛出异常
	 */
	public int executeNoQueryWithException() throws SQLException {// NO_UCD
		return executeNoQueryWithException(targetConnPool);
	}

	/**
	 * 在指定连接池上执行查询，并返回查询影响的记录数。如果发生SQL异常，则抛出异常
	 */
	public int executeNoQueryWithException(String poolName) throws SQLException {
		DataAccess da = new DataAccess(DBConnPoolManager.getConnection(poolName));
		int t = -1;
		try {
			t = da.executeNoQuery(this);
		} catch (DatabaseException e) {
			if (e.getCause() != null && e.getCause() instanceof SQLException) {
				throw (SQLException) e.getCause();
			} else {
				throw new SQLException(e.getMessage());
			}
		} finally {
			da.close();
		}
		return t;
	}

	/**
	 * 执行查询，并返回查询影响的记录数
	 */
	public int executeNoQuery() {
		return executeNoQuery(targetConnPool);
	}

	/**
	 * 在指定连接池上执行查询，并返回查询影响的记录数
	 */
	public int executeNoQuery(String poolName) {
		try {
			return executeNoQueryWithException(poolName);
		} catch (Throwable e) {
			if (e instanceof QueryException) {
				throw (QueryException) e;
			}
			throw new QueryException(e);
		}
	}

	/**
	 * 获得本查询使用的参数化SQL
	 */
	public String getSQL() {
		return sql.toString();
	}

	/**
	 * 返回当前所有SQL参数
	 */
	public ArrayList<Object> getParams() {
		return params;
	}

	/**
	 * 一次性设置所有SQL参数
	 */
	public void setParams(ArrayList<Object> list) {
		params = list;
	}

	/**
	 * 批量模式下清空所有批次
	 */
	public void clearBatches() {
		if (batchMode) {
			if (batches != null) {
				batches.clear();
			}
			batches = new ArrayList<ArrayList<Object>>();
		}
	}

	/**
	 * 检查参数化SQL中的问号个数与SQL参数个数是否相符
	 */
	public boolean checkParams() {// NO_UCD
		char[] arr = sql.toString().toCharArray();
		boolean StringCharFlag = false;
		int count = 0;
		for (char c : arr) {
			if (c == '\'') {
				if (!StringCharFlag) {
					StringCharFlag = true;
				} else {
					StringCharFlag = false;
				}
			} else if (c == '?') {
				if (!StringCharFlag) {
					count++;
				}
			}
		}
		if (count != params.size()) {
			throw new QueryException("SQL has " + count + " parameter，but value count is " + params.size());
		}
		return true;
	}

	/**
	 * 转成可读的SQL语句
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sql);
		sb.append("\t{");
		for (int i = 0; i < params.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			Object o = params.get(i);
			if (o == null) {
				sb.append("null");
				continue;
			}
			String str = params.get(i).toString();
			if (str.length() > 40) {
				str = str.substring(0, 37);
				sb.append(str);
				sb.append("...");
			} else {
				sb.append(str);
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * 执行SQL并将结果集封装成DataTable返回
	 */
	public DataTable fetch() {
		return executeDataTable();
	}

	/**
	 * 分页执行SQL并将结果集封装成DataTable返回
	 * 
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页，0为第一页
	 */
	public DataTable fetch(int pageSize, int pageIndex) {
		return executePagedDataTable(pageSize, pageIndex);
	}

	/**
	 * 在指定连接池上执行SQL并将结果集封装成DataTable返回
	 */
	public DataTable fetch(String poolName) {
		return executeDataTable(poolName);
	}

	/**
	 * 在指定连接池上分页执行SQL并将结果集封装成DataTable返回
	 * 
	 * @param pageSize 分页大小
	 * @param pageIndex 第几页，0为第一页
	 */
	public DataTable fetch(String poolName, int pageSize, int pageIndex) {// NO_UCD
		return executePagedDataTable(poolName, pageSize, pageIndex);
	}

	/**
	 * 执行SQL，返回受影响的记录数
	 */
	public int execute() {
		return executeNoQuery();
	}

	/**
	 * 在指定连接池上执行SQL，返回受影响的记录数
	 */
	public int execute(String poolName) {// NO_UCD
		return executeNoQuery(poolName);
	}

	/**
	 * 获取查询执行时的目标连接池
	 */
	public String getTargetConnPool() {
		return targetConnPool;
	}

	/**
	 * 设置查询执行时的目标连接池
	 */
	public void setTargetConnPool(String targetConnPool) {
		this.targetConnPool = targetConnPool;
	}

	/**
	 * 如果要在select时为选中的加锁，则需要调用本方法
	 */
	public void appendForUpdateLock() {
		DBConnConfig dbcc = DBConnPoolManager.getDBConnConfig(targetConnPool);
		append(DBTypeService.getInstance().get(dbcc.DBType).getForUpdate());
	}
}
