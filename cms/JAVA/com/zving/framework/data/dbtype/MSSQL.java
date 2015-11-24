package com.zving.framework.data.dbtype;

import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.command.AddColumnCommand;
import com.zving.framework.data.command.ChangeColumnLengthCommand;
import com.zving.framework.data.command.ChangeColumnMandatoryCommand;
import com.zving.framework.data.command.DropIndexCommand;
import com.zving.framework.data.command.DropTableCommand;
import com.zving.framework.data.command.RenameColumnCommand;
import com.zving.framework.data.command.RenameTableCommand;
import com.zving.framework.data.sql.SelectSQLParser;
import com.zving.framework.utility.ObjectUtil;

/**
 * SQLServer数据库(2005以上)
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-9-12
 */
public class MSSQL extends AbstractDBType {

	public final static String ID = "MSSQL";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "SQLServer 2005";
	}

	@Override
	public boolean isFullSupport() {
		return true;
	}

	@Override
	public String getJdbcUrl(DBConnConfig dcc) {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:sqlserver://");
		sb.append(dcc.DBServerAddress);
		sb.append(":");
		sb.append(dcc.DBPort);
		sb.append(";DatabaseName=");
		sb.append(dcc.DBName);
		return sb.toString();
	}

	@Override
	public void afterConnectionCreate(DBConn conn) {
	}

	@Override
	public int getDefaultPort() {
		return 1433;
	}

	@Override
	public String getPKNameFragment(String table) {
		return "constraint PK_" + table + " primary key nonclustered";
	}

	@Override
	public String toSQLType(int columnType, int length, int precision) {
		String type = null;
		if (columnType == DataTypes.BIGDECIMAL) {
			type = "numeric";
		} else if (columnType == DataTypes.BLOB) {
			type = "varbinary(MAX)";
		} else if (columnType == DataTypes.DATETIME) {
			type = "datetime";
		} else if (columnType == DataTypes.DECIMAL) {
			type = "numeric";
		} else if (columnType == DataTypes.DOUBLE) {
			type = "numeric";
		} else if (columnType == DataTypes.FLOAT) {
			type = "numeric";
		} else if (columnType == DataTypes.INTEGER) {
			type = "int";
		} else if (columnType == DataTypes.LONG) {
			type = "bigint";
		} else if (columnType == DataTypes.SMALLINT) {
			type = "smallint";
		} else if (columnType == DataTypes.STRING) {
			type = "nvarchar";// 2008以后不再支持UTF-8，必须使用nvarchar才能正常保存一些特殊字符
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
		int start = pageIndex * pageSize;
		int end = (pageIndex + 1) * pageSize;
		String sql = orginalQ.getSQL();
		SelectSQLParser sp = new SelectSQLParser(sql);
		try {
			sp.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Q q = new Q(sp.getMSSQLPagedSQL());
		q.add(orginalQ.getParams());
		q.add(start + 1);
		q.add(end);
		return q;
	}

	@Override
	public String[] toSQLArray(AddColumnCommand c) {
		String sql = "alter table " + c.Table + " add " + c.Column + " " + toSQLType(c.DataType, c.Length, c.Precision);
		if (c.Mandatory) {
			sql += " default '' not null";
		}
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(DropIndexCommand c) {
		return new String[] { "drop index " + c.Table + "." + c.Name };
	}

	@Override
	public String[] toSQLArray(DropTableCommand c) {
		String sql = "if exists (select 1 from  sysobjects where id = object_id('" + c.Table + "') and type='U') drop table " + c.Table;
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(RenameTableCommand c) {
		return new String[] { "EXEC sp_rename '" + c.Table + "', '" + c.NewTable + "'" };
	}

	@Override
	public String getDriverClass() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}

	@Override
	public String getSQLSperator() {
		return "\ngo\n";
	}

	@Override
	public String[] toSQLArray(RenameColumnCommand c) {
		return new String[] { "EXEC sp_rename '" + c.Table + "." + c.Column + "', " + c.NewColumn + ", 'column'" };
	}

	@Override
	public String[] toSQLArray(ChangeColumnLengthCommand c) {
		String sql = "alter table " + c.Table + " alter column " + c.Column + " " + toSQLType(c.DataType, c.Length, c.Precision);
		if (c.Mandatory) {
			sql += " not null";
		}
		return new String[] { sql };
	}

	@Override
	public String[] toSQLArray(ChangeColumnMandatoryCommand c) {
		String sql = "alter table " + c.Table + " alter column " + c.Column + " " + toSQLType(c.DataType, c.Length, c.Precision);
		if (c.Mandatory) {
			sql += " default '' not null";
		}
		return new String[] { sql };
	}

	@Override
	public String getForUpdate() {
		return " with(updlock, readpast)";
	}
}
