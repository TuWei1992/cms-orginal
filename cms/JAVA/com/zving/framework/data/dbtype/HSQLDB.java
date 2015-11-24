package com.zving.framework.data.dbtype;

import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.utility.ObjectUtil;

/**
 * HSqlDB支持
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-26
 */
public class HSQLDB extends AbstractDBType {

	public final static String ID = "HSQLDB";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return ID;
	}

	@Override
	public boolean isFullSupport() {
		return true;
	}

	@Override
	public String getJdbcUrl(DBConnConfig dcc) {
		StringBuilder sb = new StringBuilder(128);
		if ("file".equalsIgnoreCase(dcc.DBServerAddress)) {// 文件数据库
			sb.append("jdbc:hsqldb:res:");
			sb.append(dcc.DBName);
		} else if ("memory".equalsIgnoreCase(dcc.DBServerAddress)) {// 内存
			sb.append("jdbc:hsqldb:mem:.");
		} else {
			sb.append("jdbc:hsqldb:hsql://");
			sb.append(dcc.DBServerAddress);
			sb.append(":");
			sb.append(dcc.DBPort);
			sb.append("/");
			sb.append(dcc.DBName);
		}
		return sb.toString();
	}

	@Override
	public void afterConnectionCreate(DBConn conn) {
	}

	@Override
	public int getDefaultPort() {
		return 9001;
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
			type = "binary varying(MAX)";
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
			type = "longvarchar";
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
		Q q = new Q("select limit ? ? * from(");
		int start = pageIndex * pageSize;
		q.append(orginalQ.getSQL());
		q.append(")");
		q.add(start);
		q.add(pageSize);
		q.add(orginalQ.getParams());
		return q;
	}

	@Override
	public String getDriverClass() {
		return "org.hsqldb.jdbcDriver";
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
