package com.zving.framework.data.dbtype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.zving.framework.Config;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.command.AdvanceChangeColumnCommand;
import com.zving.framework.data.command.CreateTableCommand;
import com.zving.framework.data.command.DropIndexCommand;
import com.zving.framework.data.command.DropTableCommand;
import com.zving.framework.data.command.RenameColumnCommand;
import com.zving.framework.utility.ObjectUtil;

/**
 * MySQL数据库
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-9-12
 */
public class MySQL extends AbstractDBType {

	public final static String ID = "MYSQL";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Mysql";
	}

	@Override
	public boolean isFullSupport() {
		return true;
	}

	@Override
	public String getJdbcUrl(DBConnConfig dcc) {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:mysql://");
		sb.append(dcc.DBServerAddress);
		sb.append(":");
		sb.append(dcc.DBPort);
		sb.append("/");
		sb.append(dcc.DBName);
		return sb.toString();
	}

	@Override
	public void afterConnectionCreate(DBConn conn) {
		try {
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String charset = conn.getDBConfig().Charset;
			if (ObjectUtil.empty(charset)) {
				charset = Config.getGlobalCharset();
			}
			stmt.execute("SET NAMES '" + charset.replaceAll("\\-", "").toLowerCase() + "'");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getDefaultPort() {
		return 3306;
	}

	@Override
	public String getPKNameFragment(String table) {
		return "primary key";
	}

	@Override
	public String toSQLType(int columnType, int length, int precision) {
		String type = null;
		if (columnType == DataTypes.BIGDECIMAL) {
			type = "double";
		} else if (columnType == DataTypes.BLOB) {
			type = "longblob";
		} else if (columnType == DataTypes.DATETIME) {
			type = "datetime";
		} else if (columnType == DataTypes.DECIMAL) {
			type = "decimal";
		} else if (columnType == DataTypes.DOUBLE) {
			type = "double";
		} else if (columnType == DataTypes.FLOAT) {
			type = "float";
		} else if (columnType == DataTypes.INTEGER) {
			type = "int";
		} else if (columnType == DataTypes.LONG) {
			type = "bigint";
		} else if (columnType == DataTypes.SMALLINT) {
			type = "int";
		} else if (columnType == DataTypes.STRING) {
			type = "varchar";
		} else if (columnType == DataTypes.CLOB) {
			type = "mediumtext";
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
	public QueryBuilder getPagedQueryBuilder(DBConn conn, QueryBuilder orginalQ, int pageSize, int pageIndex) {
		Q q = new Q();
		int start = pageIndex * pageSize;
		q.append(orginalQ.getSQL()).append(" limit ?,?");
		q.add(orginalQ.getParams());
		q.add(start);
		q.add(pageSize);
		return q;
	}

	@Override
	public String[] toSQLArray(CreateTableCommand c) {
		String[] arr = c.getDefaultSQLArray(ID);
		arr[0] = arr[0] + " engine=innodb";// 指定表引擎为innodb
		return arr;
	}

	@Override
	public String[] toSQLArray(AdvanceChangeColumnCommand c) {
		String sql = "alter table " + c.Table + " modify column " + c.Column + " " + toSQLType(c.DataType, c.Length, c.Precision);
		if (c.Mandatory) {
			sql += " not null";
		}
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(DropTableCommand c) {
		String sql = "drop table if exists " + c.Table;
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(DropIndexCommand c) {
		return new String[] { "alter table " + c.Table + " drop index " + c.Name };
	}

	@Override
	public String getDriverClass() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public String getSQLSperator() {
		return ";\n";
	}

	@Override
	public String[] toSQLArray(RenameColumnCommand c) {
		return new String[] { "alter table " + c.Table + " change " + c.Column + " " + c.NewColumn + " "
				+ toSQLType(c.DataType, c.Length, c.Precision) };
	}

	@Override
	public String getForUpdate() {
		return " for update";
	}

}
