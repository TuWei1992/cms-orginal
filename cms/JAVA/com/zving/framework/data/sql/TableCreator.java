package com.zving.framework.data.sql;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.data.DBConn;
import com.zving.framework.data.DataAccess;
import com.zving.framework.data.Q;
import com.zving.framework.data.QueryException;
import com.zving.framework.data.command.CreateIndexCommand;
import com.zving.framework.data.command.CreateTableCommand;
import com.zving.framework.data.command.DropIndexCommand;
import com.zving.framework.data.command.DropTableCommand;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.orm.DAOColumn;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 数据库表创建者。<br>
 * 
 * @author 王育春
 * @date 2009-11-19
 * @mail wyuch@zving.com
 */

public class TableCreator {
	/**
	 * 待执行队列
	 */
	private ArrayList<String> list = new ArrayList<String>();

	private String dbType;

	private IDBType db;

	/**
	 * @param dbType 数据库类型
	 */
	public TableCreator(String dbType) {
		this.dbType = dbType;
		db = DBTypeService.getInstance().get(dbType);
	}

	/**
	 * 将指定表名称和字段列表的建表SQL语句放入待执行队列
	 * 
	 * @param columns 字段列表
	 * @param table 数据表名称
	 * @throws Exception
	 */
	public void createTable(DAOColumn[] columns, String table) throws Exception {
		createTable(columns, table, true);
	}

	/**
	 * 将指定表名称和字段列表的建表SQL语句放入待执行队列
	 * 
	 * @param columns 字段列表
	 * @param table 数据表名称
	 * @param dropBeforeCreate　创建之前是否先删除。如果值为false且数据库已经有表了，则会抛出异常
	 * @throws Exception
	 */
	public void createTable(DAOColumn[] columns, String table, boolean dropBeforeCreate) throws Exception {
		if (!dropBeforeCreate) {
			try {
				new Q().select("count(1)").from(table).executeInt();
			} catch (QueryException e) {
				// 抛出异常说明表不存在
				list.add(createTable(columns, table, dbType));
			}
		} else {
			dropTable(table);
			list.add(createTable(columns, table, dbType));
		}
	}

	/**
	 * @param columns 字段列表
	 * @param table 表名
	 * @param dbType 数据库类型
	 * @return 指定数据库类型下的建表语句
	 * @throws Exception
	 */
	public static String createTable(DAOColumn[] columns, String table, String dbType) throws Exception {
		IDBType db = DBTypeService.getInstance().get(dbType);
		CreateTableCommand c = new CreateTableCommand();
		c.Table = table;
		c.Columns = ObjectUtil.toList(columns);
		return db.toSQLArray(c)[0];
	}

	/**
	 * 使用指定的连接执行待执行队列中的SQL，并清空待执行列表
	 * 
	 * @param conn 数据库连接
	 */
	public void executeAndClear(DBConn conn) {
		DataAccess da = new DataAccess(conn);
		for (int i = 0; i < list.size(); i++) {
			Q q = new Q(list.get(i).toString());
			try {
				da.executeNoQuery(q);
			} catch (DatabaseException e) {
				if (q.getSQL().startsWith("drop table")) {
				} else if (q.getSQL().indexOf("drop index ") >= 0) {
				} else if (q.getSQL().indexOf("create index ") >= 0) {
					LogUtil.warn("Create index failed: " + e.getMessage().trim() + ", SQL=" + q.getSQL());
				} else {
					LogUtil.warn(q.getSQL());
					e.printStackTrace();
				}
			}
		}
		list.clear();
	}

	/**
	 * @return 返回待执行队列中的SQL
	 */
	public String[] getSQLArray() {
		String[] arr = new String[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i).toString();
		}
		return arr;
	}

	/**
	 * @return 返回以SQL语句分隔符隔开的所有待执行SQL语句
	 */
	public String getAllSQL() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i));
			sb.append(db.getSQLSperator());
		}
		return sb.toString();
	}

	/**
	 * 将删除数据表的SQL语句放到待执行队列
	 * 
	 * @param table 待删除的数据表
	 */
	public void dropTable(String table) {
		DropTableCommand c = new DropTableCommand();
		c.Table = table;
		for (String sql : db.toSQLArray(c)) {
			list.add(sql);
		}
	}

	/**
	 * 将创建索引SQL语句放入待执行队列
	 * 
	 * @param table 数据表名称
	 * @param indexInfo 索引信息，形如IndexName1:Column1,Column2;IndexName2:Column3
	 */
	public void createIndexes(String table, String indexInfo) {
		for (String sql : createIndexes(table, indexInfo, true, db)) {
			list.add(sql);
		}
	}

	/**
	 * 生成创建索引SQL语句
	 * 
	 * @param table 数据表名称
	 * @param indexInfo 索引信息，形如IndexName1:Column1,Column2;IndexName2:Column3
	 * @param dropFirst 创建之前先删除
	 * @param db 数据库类型实例
	 * @return
	 */
	public static List<String> createIndexes(String table, String indexInfo, boolean dropFirst, IDBType db) {
		if (ObjectUtil.empty(indexInfo)) {
			return new ArrayList<String>();
		}
		ArrayList<String> list = new ArrayList<String>();
		for (String str : StringUtil.splitEx(indexInfo, ";")) {
			if (ObjectUtil.empty(str)) {
				continue;
			}
			int index = str.indexOf(":");
			if (index < 1) {
				continue;
			}
			String name = str.substring(0, index);
			str = str.substring(index + 1);
			ArrayList<String> columns = new ArrayList<String>();
			for (String c : StringUtil.splitEx(str, ",")) {
				columns.add(c);
			}
			if (dropFirst) {
				DropIndexCommand dic = new DropIndexCommand();
				dic.Table = table;
				dic.Name = name;
				for (String sql : db.toSQLArray(dic)) {
					list.add(sql);
				}
			}
			CreateIndexCommand cic = new CreateIndexCommand();
			cic.Table = table;
			cic.Name = name;
			cic.Columns = columns;
			for (String sql : db.toSQLArray(cic)) {
				list.add(sql);
			}
		}
		return list;
	}

}
