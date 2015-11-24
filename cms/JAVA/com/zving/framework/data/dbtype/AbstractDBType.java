package com.zving.framework.data.dbtype;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.zving.framework.Config;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DBUtil;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.command.AddColumnCommand;
import com.zving.framework.data.command.AdvanceChangeColumnCommand;
import com.zving.framework.data.command.AlterKeyCommand;
import com.zving.framework.data.command.ChangeColumnLengthCommand;
import com.zving.framework.data.command.ChangeColumnMandatoryCommand;
import com.zving.framework.data.command.CreateIndexCommand;
import com.zving.framework.data.command.CreateTableCommand;
import com.zving.framework.data.command.DropColumnCommand;
import com.zving.framework.data.command.DropIndexCommand;
import com.zving.framework.data.command.DropTableCommand;
import com.zving.framework.data.command.IDBCommand;
import com.zving.framework.data.command.RenameColumnCommand;
import com.zving.framework.data.command.RenameTableCommand;
import com.zving.framework.utility.StringUtil;

/**
 * 数据库类型虚拟类。<br>
 * 各数据库类型应选择性地覆盖本类中的方法，以实现相应数据库类型特有的逻辑。
 * 
 * @author 王育春
 * @date 2012-12-25
 * @mail wyuch@zving.com
 */
public abstract class AbstractDBType implements IDBType {

	@Override
	public Connection createConnection(DBConnConfig dcc) throws SQLException, ClassNotFoundException {
		if (StringUtil.isEmpty(dcc.DriverClass)) {
			Class.forName(getDriverClass());
		} else {
			Class.forName(dcc.DriverClass);
		}
		if (StringUtil.isEmpty(dcc.ConnectionURL)) {
			return DriverManager.getConnection(getJdbcUrl(dcc), dcc.DBUserName, dcc.DBPassword);
		} else {
			if (StringUtil.isEmpty(dcc.DBUserName) || StringUtil.isEmpty(dcc.DBPassword)) {
				return DriverManager.getConnection(dcc.ConnectionURL);
			} else {
				return DriverManager.getConnection(dcc.ConnectionURL, dcc.DBUserName, dcc.DBPassword);
			}
		}
	}

	@Override
	public void setBlob(DBConn conn, PreparedStatement ps, int i, byte[] v) throws SQLException {
		ps.setObject(i, v);
	}

	@Override
	public void setClob(DBConn conn, PreparedStatement ps, int i, Object v) throws SQLException {
		ps.setObject(i, v);
	}

	/**
	 * @param length 字段长度
	 * @param precision 字段精度
	 * @return 字段长度和精度包装成当前数据库类型下的形式
	 */
	public static String getFieldExtDesc(int length, int precision) {
		if (length != 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			sb.append(length);
			if (precision != 0) {
				sb.append(",");
				sb.append(precision);
			}
			sb.append(") ");
			return sb.toString();
		}
		return "";
	}

	/**
	 * 将注释包装成当前数据库类型中的形式
	 */
	@Override
	public String getComment(String message) {
		String[] arr = message.split("\\n");
		StringBuilder sb = new StringBuilder();
		for (String line : arr) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append("/*");
			sb.append(line.trim());
			sb.append("*/");
		}
		return sb.toString();
	}

	/**
	 * @param dbType 数据库类型
	 * @param c 指令
	 * @return 指令在相应的数据库类型下的SQL数组
	 */
	public static String[] toSQLArray(IDBType dbType, IDBCommand c) {
		if (c instanceof CreateTableCommand) {
			return dbType.toSQLArray((CreateTableCommand) c);
		} else if (c instanceof AddColumnCommand) {
			return dbType.toSQLArray((AddColumnCommand) c);
		} else if (c instanceof AlterKeyCommand) {
			return dbType.toSQLArray((AlterKeyCommand) c);
		} else if (c instanceof AdvanceChangeColumnCommand) {
			return dbType.toSQLArray((AdvanceChangeColumnCommand) c);
		} else if (c instanceof CreateIndexCommand) {
			return dbType.toSQLArray((CreateIndexCommand) c);
		} else if (c instanceof DropColumnCommand) {
			return dbType.toSQLArray((DropColumnCommand) c);
		} else if (c instanceof DropIndexCommand) {
			return dbType.toSQLArray((DropIndexCommand) c);
		} else if (c instanceof DropTableCommand) {
			return dbType.toSQLArray((DropTableCommand) c);
		} else if (c instanceof RenameTableCommand) {
			return dbType.toSQLArray((RenameTableCommand) c);
		} else if (c instanceof RenameColumnCommand) {
			return dbType.toSQLArray((RenameColumnCommand) c);
		} else if (c instanceof ChangeColumnLengthCommand) {
			return dbType.toSQLArray((ChangeColumnLengthCommand) c);
		} else if (c instanceof ChangeColumnMandatoryCommand) {
			return dbType.toSQLArray((ChangeColumnMandatoryCommand) c);
		}
		return null;
	}

	@Override
	public String[] toSQLArray(CreateTableCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(AddColumnCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(AlterKeyCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(AdvanceChangeColumnCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(CreateIndexCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(DropColumnCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(DropIndexCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(DropTableCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(RenameTableCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(RenameColumnCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(ChangeColumnLengthCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public String[] toSQLArray(ChangeColumnMandatoryCommand c) {
		return c.getDefaultSQLArray(getExtendItemID());
	}

	@Override
	public Object getValueFromResultSet(ResultSet rs, int columnIndex, int columnType, boolean latin1Flag) throws SQLException {
		Object v = null;
		if (columnType == DataTypes.CLOB) {
			try {
				v = DBUtil.clobToString(rs.getClob(columnIndex));
			} catch (Exception e) {
				v = rs.getString(columnIndex);// sybase下getClob()不能使用，但可以用getString()
			}
		} else if (columnType == DataTypes.BLOB) {
			v = DBUtil.blobToBytes(rs.getBlob(columnIndex));
		} else if (columnType == DataTypes.DATETIME) {
			Object obj = rs.getObject(columnIndex);
			if (obj instanceof Date) {
				v = obj;
			} else {
				v = DBUtil.getOracleTimestamp(obj);
			}
		} else if (columnType == DataTypes.BIT) {
			v = "true".equals(rs.getString(columnIndex)) || "1".equals(rs.getString(columnIndex)) ? "1" : "0";
		} else if (columnType == DataTypes.STRING) {
			v = rs.getString(columnIndex);
		} else {
			v = rs.getObject(columnIndex);
			if (v instanceof BigDecimal) {
				if (columnType != DataTypes.BIGDECIMAL) {
					BigDecimal bg = (BigDecimal) v;
					if (columnType == DataTypes.LONG) {
						v = bg.longValue();
					} else {
						if (bg.scale() == 0) {
							v = bg.longValue();
						} else {
							v = bg.doubleValue();
						}
					}
				}
			}
		}
		if (latin1Flag && v != null && v instanceof String) {
			try {
				v = new String(v.toString().getBytes("ISO-8859-1"), Config.getGlobalCharset());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return v;
	}

	@Override
	public String maskColumnName(String columnName) {
		return columnName;
	}

}
