package com.zving.framework.data.dbtype;

import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.command.AddColumnCommand;
import com.zving.framework.data.command.ChangeColumnLengthCommand;
import com.zving.framework.data.command.ChangeColumnMandatoryCommand;
import com.zving.framework.data.command.DropTableCommand;
import com.zving.framework.data.command.RenameTableCommand;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * Oracle数据库
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-26
 */
public class Oracle extends AbstractDBType {

	public final static String ID = "ORACLE";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Oracle";
	}

	@Override
	public boolean isFullSupport() {
		return true;
	}

	@Override
	public String getJdbcUrl(DBConnConfig dcc) {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:oracle:thin:@");
		sb.append(dcc.DBServerAddress);
		sb.append(":");
		sb.append(dcc.DBPort);
		sb.append(":");
		sb.append(dcc.DBName);
		return sb.toString();
	}

	@Override
	public Connection createConnection(DBConnConfig dcc) throws SQLException, ClassNotFoundException {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Properties props = new Properties();
		props.setProperty("user", dcc.DBUserName);
		props.setProperty("password", dcc.DBPassword);
		props.setProperty("oracle.jdbc.V8Compatible", "true");// oracle10g date类型丢失时间精度

		return DriverManager.getConnection(StringUtil.isEmpty(dcc.ConnectionURL) ? getJdbcUrl(dcc) : dcc.ConnectionURL, props);

	}

	@Override
	public void afterConnectionCreate(DBConn conn) throws SQLException {
		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		stmt.execute("alter session set nls_date_format = 'YYYY-MM-DD HH24:MI:SS'");
		stmt.close();
	}

	@Override
	public int getDefaultPort() {
		return 1521;
	}

	public String getTableDropSQL(String tableCode) {
		return "drop table " + tableCode + " cascade constraints";
	}

	@Override
	public String toSQLType(int columnType, int length, int precision) {
		String type = null;
		if (columnType == DataTypes.BIGDECIMAL) {
			type = "DOUBLE PRECISION";
		} else if (columnType == DataTypes.BLOB) {
			type = "BLOB";
		} else if (columnType == DataTypes.DATETIME) {
			type = "DATE";
		} else if (columnType == DataTypes.DECIMAL) {
			type = "DECIMAL";
		} else if (columnType == DataTypes.DOUBLE) {
			type = "NUMBER";
		} else if (columnType == DataTypes.FLOAT) {
			type = "FLOAT";
		} else if (columnType == DataTypes.INTEGER) {
			type = "INTEGER";
		} else if (columnType == DataTypes.LONG) {
			type = "INTEGER";
		} else if (columnType == DataTypes.SMALLINT) {
			type = "INTEGER";
		} else if (columnType == DataTypes.STRING) {
			type = "VARCHAR2";
		} else if (columnType == DataTypes.CLOB) {
			type = "CLOB";
		}
		if (ObjectUtil.empty(type)) {
			throw new RuntimeException("Unknown DBType " + getExtendItemID() + " or DataType" + columnType);
		}
		if (length == 0 && columnType == DataTypes.STRING) {
			throw new RuntimeException("varchar's length can't be empty!");
		}
		return type + getFieldExtDesc(length, precision);
	}

	@Override
	public void setBlob(DBConn conn, PreparedStatement ps, int i, byte[] v) throws SQLException {
		Class<?> blobClass = null;
		try {
			blobClass = Class.forName("oracle.sql.BLOB");
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			return;
		}
		Object blob = null;
		Object oc = conn.getPhysicalConnection();
		try {
			Method m = blobClass.getMethod("createTemporary", new Class[] { java.sql.Connection.class, boolean.class, int.class });
			blob = m.invoke(null, new Object[] { oc, new Boolean(true), new Integer(1) });
			// 相当于blob=BLOB.createTemporary(oc,true,1);
			// Oracle9i中是1,10G中变成了10，但还是会将1自动转为10

			m = blobClass.getMethod("open", new Class[] { int.class });
			m.invoke(blob, new Object[] { new Integer(1) });// 相当于blob.open(1);

			m = blobClass.getMethod("getBinaryOutputStream", new Class[] { long.class });
			OutputStream out = (OutputStream) m.invoke(blob, new Object[] { new Long(0) });
			out.write(v);
			out.close();

			blobClass.getMethod("close", (Class[]) null).invoke(blob, (Object[]) null);// 相当于blob.close();
			ps.setBlob(i, (Blob) blob);
		} catch (Exception e) {
			try {
				if (blob != null) {
					Method m = blobClass.getMethod("freeTemporary", new Class[] { blobClass });
					m.invoke(null, new Object[] { blob });// 相当于BLOB.freeTemporary(clob);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	@Override
	public void setClob(DBConn conn, PreparedStatement ps, int i, Object v) throws SQLException {
		Class<?> clobClass = null;
		try {
			clobClass = Class.forName("oracle.sql.CLOB");
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			return;
		}
		Object clob = null;
		Object oc = conn.getPhysicalConnection();
		try {
			Method m = clobClass.getMethod("createTemporary", new Class[] { java.sql.Connection.class, boolean.class, int.class });
			clob = m.invoke(null, new Object[] { oc, new Boolean(true), new Integer(1) });
			// 相当于clob=CLOB.createTemporary(oc,true,1);
			// Oracle9i中是1,10G中变成了10，但还是会将1自动转为10

			m = clobClass.getMethod("open", new Class[] { int.class });
			m.invoke(clob, new Object[] { new Integer(1) });// 相当于clob.open(1);

			m = clobClass.getMethod("setCharacterStream", new Class[] { long.class });
			Writer writer = (Writer) m.invoke(clob, new Object[] { new Long(0) });
			// 相当于Writer writer = clob.setCharacterStream(0L);
			writer.write(ObjectUtil.toString(v));
			writer.close();

			clobClass.getMethod("close", (Class[]) null).invoke(clob, (Object[]) null);// 相当于clob.close();
			ps.setClob(i, (Clob) clob);
		} catch (Exception e) {
			try {
				if (clob != null) {
					Method m = clobClass.getMethod("freeTemporary", new Class[] { clobClass });
					m.invoke(null, new Object[] { clob });// 相当于CLOB.freeTemporary(clob);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	@Override
	public QueryBuilder getPagedQueryBuilder(DBConn conn, QueryBuilder orginalQ, int pageSize, int pageIndex) {
		Q q = new Q();
		int start = pageIndex * pageSize;
		int end = (pageIndex + 1) * pageSize;
		q.append("select * from (select rs.*,rownum rnm from (");
		q.append(orginalQ.getSQL());
		q.append(") rs where rownum<=?) rss where rnm>?");
		q.add(orginalQ.getParams());
		q.add(end);
		q.add(start);
		return q;
	}

	@Override
	public String getDriverClass() {
		return "oracle.jdbc.driver.OracleDriver";
	}

	@Override
	public String[] toSQLArray(AddColumnCommand c) {
		String sql = "alter table " + c.Table + " add " + c.Column + " " + toSQLType(c.DataType, c.Length, c.Precision);
		if (c.Mandatory) {
			sql += " not null";
		}
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(DropTableCommand c) {
		String sql = "drop table " + c.Table + " cascade constraints";
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(RenameTableCommand c) {
		return new String[] { "rename " + c.Table + " to " + c.NewTable };
	}

	@Override
	public String getPKNameFragment(String table) {
		return "constraint PK_" + table + " primary key";
	}

	@Override
	public String getSQLSperator() {
		return ";\n";
	}

	@Override
	public String[] toSQLArray(ChangeColumnLengthCommand c) {
		String sql = "alter table " + c.Table + " modify " + c.Column + " " + toSQLType(c.DataType, c.Length, c.Precision);
		if (c.Mandatory) {
			sql += " not null";
		}
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(ChangeColumnMandatoryCommand c) {
		String sql = "alter table " + c.Table + " modify " + c.Column + " " + toSQLType(c.DataType, c.Length, c.Precision);
		if (c.Mandatory) {
			sql += " not null";
		}
		return new String[] { sql };
	}

	@Override
	public String getForUpdate() {
		return " for update";
	}

}
