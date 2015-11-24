package com.zving.framework.data;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.dbtype.DBTypeService;

/**
 * 数据库工具库
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2008-3-12
 */
public class DBUtil {

	/**
	 * CLOB转换为字符串
	 */
	public static String clobToString(Clob clob) {
		if (clob == null) {
			return null;
		}
		try {
			Reader r = clob.getCharacterStream();
			StringWriter sw = new StringWriter();
			char[] cs = new char[(int) clob.length()];
			try {
				r.read(cs);
				sw.write(cs);
				return sw.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * BLOB转换为字节
	 */
	public static byte[] blobToBytes(Blob blob) {
		if (blob == null) {
			return null;
		}
		try {
			return blob.getBytes(1L, (int) blob.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取数据表信息
	 */
	public static DataTable getTableInfo() {
		return getTableInfo(DBConnPoolManager.getDBConnConfig());
	}

	/**
	 * 按指定的连接配置获取数据表信息
	 */
	public static DataTable getTableInfo(DBConnConfig dcc) {
		DBConn conn = null;
		try {
			conn = DBConnPool.createConnection(dcc, false);
			DatabaseMetaData dbm = conn.getMetaData();
			String currentCatalog = conn.getCatalog();
			ResultSet rs = dbm.getTables(currentCatalog, null, null, null);
			DataTable dt = new DataTable(conn, rs);
			for (DataRow dr : dt) {
				if (dr.get(1) != null) {
					dr.set(2, dr.getString(1) + "." + dr.getString(2));
				}
			}
			return dt;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 获取指定数据库下的指定SQL的列信息
	 */
	public static DataTable getColumnInfo(DBConnConfig dcc, String tableName) {
		DBConn conn = null;
		try {
			conn = DBConnPool.createConnection(dcc, false);
			DatabaseMetaData dbm = conn.getMetaData();
			String currentCatalog = conn.getCatalog();
			String schema = null;
			String oldName = tableName;
			int index = tableName.indexOf(".");
			if (index > 0) {
				schema = tableName.substring(0, index);
				tableName = tableName.substring(index + 1);
			}
			if (schema == null) {
				schema = dcc.DBUserName;
			}
			//如果是sqlserver2005，必须置为null，否则查不出表
			if (dcc.isSQLServer()) {
				schema=null;
			} else {
				schema=schema.toLowerCase();
			}
			ResultSet rs = dbm.getColumns(currentCatalog, schema, tableName.toUpperCase(), null);
			DataTable dt = new DataTable(conn, rs);

			rs = dbm.getPrimaryKeys(currentCatalog, null, tableName);
			DataTable keyDt = new DataTable(conn, rs);
			Mapx<String, Object> map = keyDt.toMapx("Column_Name", "PK_Name");
			dt.insertColumn("isKey");
			for (int i = 0; i < dt.getRowCount(); i++) {
				DataRow dr = dt.getDataRow(i);
				if (map.containsKey(dr.getString("Column_Name"))) {
					dr.set("isKey", "Y");
				} else {
					dr.set("isKey", "N");
				}
			}
			DataAccess da = new DataAccess(conn);
			DataTable data = da.executeDataTable(new Q().select("*").from(oldName).where("1", "2"));
			for (int i = 0; i < data.getColumnCount(); i++) {
				DataRow dr = dt.getDataRow(i);
				dr.set("Type_Name", data.getDataColumn(i).getColumnType());
			}
			return dt;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 根据传入的sql语句计算总条数
	 */
	public static int getCount(QueryBuilder qb) {
		return getCount(qb, null);
	}

	/**
	 * 获取记录条数
	 */
	public static int getCount(QueryBuilder qb, String poolName) {
		DataAccess da = new DataAccess(DBConnPoolManager.getConnection(poolName));
		try {
			return da.getCount(qb);
		} finally {
			da.close();
		}
	}

	/**
	 * 获取Oracle时间戳
	 */
	public static Timestamp getOracleTimestamp(Object value) {
		if (value == null) {
			return null;
		}
		try {
			Class<?> clz = value.getClass();
			Method m = clz.getMethod("timestampValue", (Class[]) null);
			return (Timestamp) m.invoke(value, (Object[]) null);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取bitand函数
	 */
	public static String sqlBitAndFunction(String columnName, String n) {
		return sqlBitAndFunction(columnName, n, null);
	}

	/**
	 * 获取Bitand函数
	 */
	public static String sqlBitAndFunction(String columnName, int n) {
		return sqlBitAndFunction(columnName, n + "", null);
	}

	/**
	 * 获取bitand函数
	 */
	public static String sqlBitAndFunction(String columnName, String n, String poolName) {
		DBConnConfig dcc = DBConnPoolManager.getDBConnConfig(poolName);
		if (dcc.isOracle() || dcc.isDB2()) {
			return "bitand(" + columnName + "," + n + ")";
		} else {
			return columnName + "&" + n;
		}
	}

	/**
	 * SQL语句是否是一个for update式的结束
	 */
	public static boolean isEndsWithForUpdate(DBConnConfig dbcc, String sql) {
		sql = sql.trim().replaceAll("\\s+", " ").toLowerCase();
		if (!sql.startsWith("select")) {
			return false;
		}
		int i = sql.lastIndexOf('\'');
		if (dbcc.isOracle() || dbcc.isMysql()) {
			return i < 0 || sql.lastIndexOf("for update") > i;
		} else if (dbcc.isDB2() || dbcc.isSQLServer() || dbcc.isSQLServer2000() || dbcc.isSybase()) {
			return i < 0 || sql.lastIndexOf("with(") > i || sql.lastIndexOf("with (") > i;
		} else {
			String forUpdate = DBTypeService.getInstance().get(dbcc.DBType).getForUpdate();
			if (forUpdate.length() > 0 && sql.endsWith(forUpdate)) {
				return true;
			}
		}
		return false;
	}
}
