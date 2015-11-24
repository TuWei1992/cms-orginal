package com.zving.framework.data.dbtype;

import com.zving.framework.data.DBConnConfig;

/**
 * Derby服务器模式数据库
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-26
 */
public class DerbyServer extends DerbyEmbedded {

	public final static String ID = "DerbyServer";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Derby Server";
	}

	@Override
	public String getJdbcUrl(DBConnConfig dcc) {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:derby://");
		sb.append(dcc.DBServerAddress);
		sb.append(":");
		sb.append(dcc.DBPort);
		sb.append("/");
		sb.append(dcc.DBName);
		sb.append(";create=true");
		return sb.toString();
	}

	@Override
	public String getDriverClass() {
		return "org.apache.derby.jdbc.ClientDriver";
	}

}
