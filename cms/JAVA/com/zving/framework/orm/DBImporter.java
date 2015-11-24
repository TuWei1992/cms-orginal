package com.zving.framework.orm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.zving.framework.ConfigLoader;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnPoolManager;
import com.zving.framework.data.DataAccess;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.data.sql.TableCreator;
import com.zving.framework.orm.ZDTParser.ZDTTableInfo;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;

/**
 * 数据库导入类
 * 
 * @Author 王育春
 * @Date 2008-10-13
 * @Mail wyuch@zving.com
 */
public class DBImporter {
	private DataAccess da;

	private LongTimeTask task;

	public void setTask(LongTimeTask task) {
		this.task = task;
	}

	public String getSQL(String file, String dbtype) {
		TableCreator tc = new TableCreator(dbtype);
		try {
			ZDTParser parser = new ZDTParser(file);
			parser.parse();
			for (ZDTTableInfo ti : parser.getTables()) {
				tc.createTable(ti.Columns, ti.Name);
				tc.createIndexes(ti.Name, ti.IndexInfo);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return tc.getAllSQL();
	}

	public void importDB(String file) {
		importDB(file, "", null);
	}

	public boolean importDB(String file, String poolName, List<String> tableList) {
		DBConn conn = DBConnPoolManager.getConnection(poolName);
		try {
			return importDB(file, conn, true, tableList);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean importDB(String file, DBConn conn, boolean dropBeforeCreate, List<String> tableList) {
		da = new DataAccess(conn);
		try {
			ZDTParser parser = new ZDTParser(file);
			parser.parse();
			Mapx<String, String> map = new Mapx<String, String>();
			TableCreator tc = new TableCreator(da.getConnection().getDBConfig().DBType);
			int j = 0;
			for (ZDTTableInfo ti : parser.getTables()) {
				if (tableList != null && !tableList.contains(ti.Name)) {
					continue;
				}
				if (!map.containsKey(ti.Name)) {
					tc.createTable(ti.Columns, ti.Name, dropBeforeCreate);
					tc.executeAndClear(conn);
					map.put(ti.Name, "");
				}
				if (task != null) {
					task.setPercent(new Double(++j * 100.0 / parser.getTables().size()).intValue());
				}
				if (task != null) {
					task.setCurrentInfo("Importing table " + ti.Name);
				}
				String version = parser.getVersion();
				if (ZDTParser.VERSION_1.equals(version) || ZDTParser.VERSION_2.equals(version)) {
					for (int i = 0; i < ti.Positions.size(); i++) {
						DataTable dt = parser.getDataTable(ti, i * DBExporter.PageSize, (i + 1) * DBExporter.PageSize);
						try {
							if (!importDataTable(ti.Columns, dt, ti.Name)) {
								return false;
							}
						} catch (Exception e) {
							LogUtil.warn("Import table failed:" + ti.Name);
							e.printStackTrace();
						}
					}
				} else if (ZDTParser.VERSION_3.equals(version)) {
					if (!parser.importDB(ti, da)) {
						return false;
					}
				}
				// 如果有索引信息，则创建索引
				if (ObjectUtil.notEmpty(ti.IndexInfo) && !ti.IndexInfo.trim().equals("")) {
					tc.createIndexes(ti.Name, ti.IndexInfo);
					try {
						tc.executeAndClear(conn);
					} catch (DatabaseException e) {
						LogUtil.warn(e.getMessage());
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 对3以上版本无效
	 * @param scs
	 * @param dt
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private boolean importDataTable(DAOColumn[] scs, DataTable dt, String name) throws Exception {
		IDBType db = DBTypeService.getInstance().get(this.da.getConnection().getDBConfig().DBType);
		List<String> columns = new ArrayList<String>(scs.length);
		for (DAOColumn sc : scs) {
			columns.add(db.maskColumnName(sc.getColumnName()));
		}
		if (da.getConnection().getDBConfig().isOracle()) {// 需要检查，Mysql
			// NotNull的列允许空字符串，但Oracle不允许
			for (int i = 0; i < dt.getRowCount(); i++) {
				for (int j = 0; j < scs.length; j++) {
					Object v = dt.get(i, j);
					if (scs[j].isMandatory() && (v == null || v.equals(""))) {
						LogUtil.warn(name + "'s column " + scs[j].getColumnName() + " can't be empty");
						dt.deleteRow(i);
						i--;
						break;
					}
				}
			}
		}
		Q q = new Q();
		q.setBatchMode(true);
		for (DataRow dr : dt) {
			List<Object> values = new ArrayList<Object>(scs.length);
			for (int i = 0; i < scs.length; i++) {
				values.add(dr.get(i));
			}
			if (q.getSQL().length() == 0) {
				q.insertInto(name, columns, values);
			} else {
				q.add(values);
			}
			q.addBatch();
		}
		if (dt.getRowCount() > 0) {
			da.executeNoQuery(q);
		}
		return true;
	}

	public static void main(String[] args) {
		if (ObjectUtil.empty(args)) {
			return;
		}
		ConfigLoader.load("F:/ZWorkspace/ZCMS2/JAVA/");
		long t1 = System.currentTimeMillis();
		new DBImporter().importDB(args[0]);
		long t2 = System.currentTimeMillis();
		System.out.println("现在：" + (t2 - t1));
	}
}
