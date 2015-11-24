package com.zving.framework.data.dbtype;

import java.sql.SQLException;

import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.command.DropTableCommand;
import com.zving.framework.data.command.RenameTableCommand;
import com.zving.framework.utility.ObjectUtil;

/**
 * 达梦数据库，从7.1开始支持
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-10-29
 */
public class DM extends AbstractDBType {

	public final static String ID = "DM";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "DaMeng";
	}

	@Override
	public boolean isFullSupport() {
		return true;
	}

	@Override
	public String getJdbcUrl(DBConnConfig dcc) {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:dm://");
		sb.append(dcc.DBServerAddress);
		sb.append(":");
		sb.append(dcc.DBPort);
		sb.append("/");
		sb.append(dcc.DBName);
		return sb.toString();
	}

	@Override
	public int getDefaultPort() {
		return 5236;
	}

	public String getTableDropSQL(String tableCode) {
		return "drop table " + tableCode + " cascade";
	}

	@Override
	public String toSQLType(int columnType, int length, int precision) {
		String type = null;
		if (columnType == DataTypes.BIGDECIMAL) {
			type = "decimal";
		} else if (columnType == DataTypes.BLOB) {
			type = "blob";
		} else if (columnType == DataTypes.DATETIME) {
			type = "datetime";
		} else if (columnType == DataTypes.DECIMAL) {
			type = "decimal";
		} else if (columnType == DataTypes.DOUBLE) {
			type = "double";
		} else if (columnType == DataTypes.FLOAT) {
			type = "float";
		} else if (columnType == DataTypes.INTEGER) {
			type = "integer";
		} else if (columnType == DataTypes.LONG) {
			type = "bigint";
		} else if (columnType == DataTypes.SMALLINT) {
			type = "short";
		} else if (columnType == DataTypes.STRING) {
			type = "varchar";
		} else if (columnType == DataTypes.CLOB) {
			type = "text";
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
		String sql = orginalQ.getSQL();
		if (!sql.substring(0, 7).equalsIgnoreCase("select ")) {
			return orginalQ;
		}
		Q q = new Q("select top ?,? ");
		q.append(sql.substring(7));
		int start = pageIndex * pageSize;
		int end = (pageIndex + 1) * pageSize;
		q.add(start);
		q.add(end);
		q.add(orginalQ.getParams());
		return q;
	}

	@Override
	public String getDriverClass() {
		return "dm.jdbc.driver.DmDriver";
	}

	@Override
	public String[] toSQLArray(DropTableCommand c) {
		String sql = "drop table " + c.Table + " cascade";
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(RenameTableCommand c) {
		return new String[] { "rename " + c.Table + " to " + c.NewTable };
	}

	@Override
	public String getPKNameFragment(String table) {
		return "primary key";
	}

	@Override
	public String getSQLSperator() {
		return ";\n";
	}

	@Override
	public String getForUpdate() {
		return " for update";
	}

	@Override
	public void afterConnectionCreate(DBConn conn) throws SQLException {
	}

	@Override
	public String maskColumnName(String columnName) {
		if ("versions".equalsIgnoreCase(columnName)) {
			columnName = "\"" + columnName + "\"";
		}
		return columnName;
	}
}
