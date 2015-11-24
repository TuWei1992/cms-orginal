package com.zving.framework.data.dbtype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.sql.SelectSQLParser;

/**
 * Sybase ASE数据库
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-9-12
 */
public class Sybase extends MSSQL {

	public final static String ID = "SYBASE";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Sybase";
	}

	@Override
	public boolean isFullSupport() {
		return true;
	}

	@Override
	public String getJdbcUrl(DBConnConfig dcc) {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:sybase:Tds:");
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
			stmt.execute("set textsize 20971520");// 防止text字段超32K后，32K之后的部分不能取出来
			stmt.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public int getDefaultPort() {
		return 5000;
	}

	@Override
	public QueryBuilder getPagedQueryBuilder(DBConn conn, QueryBuilder orginalQ, int pageSize, int pageIndex) {
		String sql = orginalQ.getSQL();
		SelectSQLParser sp = new SelectSQLParser(sql);
		try {
			sp.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Q q = new Q(sp.getSybasePagedSQL(pageSize, pageIndex, DBConn.getConnID()));
		q.add(orginalQ.getParams());
		return q;
	}

	@Override
	public String getDriverClass() {
		return "com.sybase.jdbc3.jdbc.SybDriver";
	}

	@Override
	public String getForUpdate() {
		return " ";
	}

	@Override
	public Object getValueFromResultSet(ResultSet rs, int columnIndex, int columnType, boolean latin1Flag) throws SQLException {
		Object v = super.getValueFromResultSet(rs, columnIndex, columnType, latin1Flag);
		if (v != null && (columnType == DataTypes.STRING || columnType == DataTypes.CLOB)) {
			if (v.equals(" ")) {
				v = "";// 这是sybase的一个问题，null或者空字符串会返回一个空格。
			}
		}
		return v;
	}
}
