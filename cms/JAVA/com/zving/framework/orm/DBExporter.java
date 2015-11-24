package com.zving.framework.orm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.annotation.dao.NotExport;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnPoolManager;
import com.zving.framework.data.DataAccess;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.orm.ZDTParser.ZDTTableInfo;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 数据库导出类
 * 
 * @Author 王育春
 * @Date 2008-10-13
 * @Mail wyuch@zving.com
 */
public class DBExporter {

	public final static int PageSize = 500;

	private DataAccess da;

	private RandomAccessFile braf;

	private LongTimeTask task;

	private static final String ZDMVersion1 = "3";

	private static String CurrentVersion = ZDMVersion1;

	private ArrayList<ZDTTableInfo> Tables;

	public void setTask(LongTimeTask task) {
		this.task = task;
	}

	public void setTables(ArrayList<ZDTTableInfo> tables) {
		Tables = tables;
	}

	public void exportDB(String file) {
		exportDB(file, DBConnPoolManager.DEFAULT_POOLNAME, null);
	}

	public void exportDB(String file, String poolName, ArrayList<String> tableList) {
		DBConn conn = DBConnPoolManager.getConnection(poolName,true);
		try {
			exportDB(file, conn, tableList);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void exportDB(String file, DBConn conn, ArrayList<String> list) {
		// 创建目录
		String dir = FileUtil.normalizePath(file);
		File dFile = new File(dir.substring(0, dir.lastIndexOf("/") + 1));
		if (!dFile.exists()) {
			dFile.mkdirs();
		}
		FileUtil.delete(file);
		da = new DataAccess(conn);
		try {
			braf = new RandomAccessFile(file, "rw");
			// 先写入文件版本
			byte[] bs = CurrentVersion.getBytes();
			braf.write(NumberUtil.toBytes(bs.length));
			braf.write(bs);
			if (Tables == null) {
				Tables = getTableListFromClass();
			}
			for (int i = 0; i < Tables.size(); i++) {
				try {
					ZDTTableInfo table = Tables.get(i);
					if (task != null) {
						task.setPercent(new Double(i * 100.0 / Tables.size()).intValue());
						task.setCurrentInfo("Exporting table " + table.Name);
					}
					String tableCode = Tables.get(i).Name;
					if (list == null || list.contains(tableCode)) {
						transferOneTable(tableCode, table.IndexInfo, table.Columns, conn);
						transferOneTable("B" + tableCode, null, DAOUtil.addBackupColumn(table.Columns), conn);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (braf != null) {
				try {
					braf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			da.close();
		}
	}

	private ArrayList<ZDTTableInfo> getTableListFromClass() {
		String[] arr = DAOUtil.getAllDAOClassName();
		ArrayList<ZDTTableInfo> list = new ArrayList<ZDTTableInfo>();
		for (String daoClassName : arr) {
			ZDTTableInfo ti = new ZDTTableInfo();
			try {
				Class<?> clazz = Class.forName(daoClassName);
				DAO<?> dao = (DAO<?>) clazz.newInstance();
				NotExport ne = clazz.getAnnotation(NotExport.class);
				if (ne != null && ne.value()) {
					continue;// 加了@NotExport注解的DAO不导出
				}
				String name = DAOUtil.getTableCode(dao);
				ti.Name = name;
				ti.IndexInfo = dao.indexInfo();
				ti.Columns = dao.columns();
				list.add(ti);
			} catch (Exception e) {// 没有Schema对应的表
				LogUtil.warn("DAO's target table not found:" + daoClassName);
			}
		}
		return list;
	}

	private void transferOneTable(String name, String indexInfo, DAOColumn[] scs, DBConn conn) throws Exception {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			IDBType db = DBTypeService.getInstance().get(da.getConnection().getDBConfig().DBType);
			List<String> columns = new ArrayList<String>();
			for (DAOColumn sc : scs) {
				columns.add(db.maskColumnName(sc.getColumnName()));
			}
			Q q = new Q().select(columns).from(name);
			int rowCount = 0;
			String sql = q.getSQL();
			boolean latin1Flag = conn.getDBConfig().isLatin1Charset && conn.getDBConfig().isOracle();
			if (latin1Flag) {
				try {
					sql = new String(sql.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			DataAccess.setParams(stmt, q, conn);
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			// 先写入名称
			byte[] bs = name.getBytes();
			braf.write(NumberUtil.toBytes(bs.length));
			braf.write(bs);
			// 写入字段描述
			bs = getColumnString(scs).getBytes();
			braf.write(NumberUtil.toBytes(bs.length));
			braf.write(bs);
			// 写入索引信息
			if (ObjectUtil.empty(indexInfo)) {
				indexInfo = Constant.Null;
			}
			bs = indexInfo.getBytes();
			braf.write(NumberUtil.toBytes(bs.length));
			braf.write(bs);
			long rowCountPos = braf.getFilePointer();
			braf.writeInt(rowCount);
			// 以下准备ColumnValues[]
			for (rowCount = 0; rs.next(); rowCount++) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < columnCount; j++) {
					int columnType = scs[j].getColumnType();
					Object rowValue = db.getValueFromResultSet(rs, j + 1, columnType, latin1Flag);
					if (j != 0) {
						sb.append("\t");
					}
					if (columnType == DataTypes.BLOB) {
						sb.append(StringUtil.javaEncode(StringUtil.base64Encode((byte[]) rowValue)));
					} else if (ObjectUtil.in(scs[j].getColumnType(), DataTypes.STRING, DataTypes.CLOB)) {
						if (rowValue == null) {
							sb.append("null");
						} else {
							sb.append("\"");
							sb.append(StringUtil.javaEncode(String.valueOf(rowValue)));
							sb.append("\"");
						}
					} else if (scs[j].getColumnType() == DataTypes.DATETIME) {
						if (rowValue == null) {
							sb.append("");
						} else {
							sb.append(DateUtil.toDateTimeString((Date) rowValue));
						}
					} else {
						if (rowValue == null) {
							sb.append("");
						} else {
							sb.append(String.valueOf(rowValue));
						}
					}
				}
				// 写入数据
				bs = sb.toString().trim().getBytes("UTF-8");
				braf.writeInt(bs.length);
				braf.write(bs);
			}
			long currentPos = braf.getFilePointer();
			// 写入总条数
			braf.seek(rowCountPos);
			braf.writeInt(rowCount);
			// 跳至数据末尾
			braf.seek(currentPos);
		} catch (Exception e) {// 没有Schema对应的表
			LogUtil.warn("Table not found:" + name + ";" + e.getMessage());
			return;
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getColumnString(DAOColumn[] scs) {
		StringBuilder sb = new StringBuilder();
		for (DAOColumn sc : scs) {
			sb.append(sc.getColumnName());
			sb.append("\t");
			sb.append(sc.getColumnType());
			sb.append("\t");
			sb.append(sc.getLength());
			sb.append("\t");
			sb.append(sc.getPrecision());
			sb.append("\t");
			sb.append(sc.isMandatory());
			sb.append("\t");
			sb.append(sc.isPrimaryKey());
			sb.append("\n");
		}
		return sb.toString().trim();
	}

}
