package com.zving.framework.data.dbtype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.utility.ObjectUtil;

/**
 * Derby嵌入式数据库
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-26
 */
public class DerbyEmbedded extends AbstractDBType {

	public final static String ID = "DerbyEmbedded";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Derby Embedded";
	}

	@Override
	public boolean isFullSupport() {
		return true;
	}

	@Override
	public String getJdbcUrl(DBConnConfig dcc) {
		return "jdbc:derby:" + dcc.DBName.replace('\\', '/') + ";create=true";
	}

	@Override
	public void afterConnectionCreate(DBConn conn) {
		try {
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			stmt.execute("set schema app");// 将当前schema设为app
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getDefaultPort() {
		return 1527;
	}

	@Override
	public String getPKNameFragment(String table) {
		return "primary key";
	}

	@Override
	public String toSQLType(int columnType, int length, int precision) {
		String type = null;
		if (columnType == DataTypes.BIGDECIMAL) {
			type = "numeric";
		} else if (columnType == DataTypes.BLOB) {
			type = "blob";
		} else if (columnType == DataTypes.DATETIME) {
			type = "timestamp";
		} else if (columnType == DataTypes.DECIMAL) {
			type = "decimal";
		} else if (columnType == DataTypes.DOUBLE) {
			type = "double";
		} else if (columnType == DataTypes.FLOAT) {
			type = "double";
		} else if (columnType == DataTypes.INTEGER) {
			type = "integer";
		} else if (columnType == DataTypes.LONG) {
			type = "bigint";
		} else if (columnType == DataTypes.SMALLINT) {
			type = "smallint";
		} else if (columnType == DataTypes.STRING) {
			type = "varchar";
		} else if (columnType == DataTypes.CLOB) {
			type = "clob";
		}
		if (ObjectUtil.empty(type)) {
			throw new RuntimeException("Unknown DBType " + getExtendItemID() + " or DataType" + columnType);
		}
		if (length == 0 && columnType == DataTypes.STRING) {
			throw new RuntimeException("varchar's length can't be empty!");
		}

		String FieldExtDesc = "";
		if (length != 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			sb.append(length);
			if (precision != 0) {
				sb.append(",");
				sb.append(precision);
			}
			sb.append(") ");
			FieldExtDesc = sb.toString();
		}

		return type + FieldExtDesc;
	}

	@Override
	public QueryBuilder getPagedQueryBuilder(DBConn conn, QueryBuilder orginalQ, int pageSize, int pageIndex) {
		int start = pageIndex * pageSize;
		Q q = new Q(orginalQ.getSQL());
		q.append(" offset ? rows fetch next ? rows only");
		q.add(orginalQ.getParams());
		q.add(start);
		q.add(pageSize);
		return q;
	}

	@Override
	public String getDriverClass() {
		return "org.apache.derby.jdbc.EmbeddedDriver";
	}

	@Override
	public String getSQLSperator() {
		return ";\n";
	}

	@Override
	public String getForUpdate() {
		return "";
	}
}
