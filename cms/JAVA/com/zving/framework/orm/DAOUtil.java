package com.zving.framework.orm;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.zving.framework.Config;
import com.zving.framework.core.scanner.BuiltResource;
import com.zving.framework.core.scanner.BuiltResourceScanner;
import com.zving.framework.core.scanner.IBuiltResourceVisitor;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.thirdparty.asm.Opcodes;
import com.zving.framework.thirdparty.asm.tree.ClassNode;
import com.zving.framework.utility.StringUtil;

/**
 * DAO和DAOSet操作的工具类<br>
 * 作者：王育春<br>
 * 日期：2006-7-3<br>
 * 邮件：wyuch@zving.com<br>
 */
public class DAOUtil {
	private static AtomicLong BackupNoBase = new AtomicLong(System.currentTimeMillis());

	/**
	 * 从B表DAO中获得Z表DAO
	 */
	public static <T extends DAO<T>> T getDAOFromBackupDAO(BackupDAO<T> bDAO) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> c = (Class<T>) bDAO.getDAO().getClass();
			T dao = c.newInstance();
			for (int i = 0; i < dao.columns().length; i++) {
				dao.setV(i, bDAO.getV(i));
			}
			return dao;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从B表Set中获得Z表Set，注意B表中多条记录转换后可能只有一条Z表记录<br>
	 * 会在转换后的记录中去除掉主键相同的多余记录，只保留BackupNo最大的一条记录
	 * 
	 * @since 1.3
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DAO<T>> DAOSet<T> getDAOSetFromBackupDAOSet(DAOSet<BackupDAO<T>> bset) {
		if (bset.size() == 0) {
			return new DAOSet<T>();
		}
		try {
			bset.sort("BackupNo", "asc");
			// 得到主键顺序
			ArrayList<Integer> list = new ArrayList<Integer>();
			DAOColumn[] columns = bset.get(0).columns();
			for (int i = 0; i < columns.length; i++) {
				if (columns[i].isPrimaryKey() && !columns[i].getColumnName().equalsIgnoreCase("BackupNo")) {
					list.add(i);
				}
			}
			int[] keys = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				keys[i] = list.get(i).intValue();
			}
			for (int i = 0; i < bset.size(); i++) {
				// 取出当前行的主键
				Object[] ks = new Object[keys.length];
				for (int j = 0; j < ks.length; j++) {
					ks[j] = bset.get(i).getV(j);
				}
				for (int j = i + 1; j < bset.size();) {
					boolean flag = true;
					for (int k = 0; k < keys.length; k++) {
						if (!bset.get(j).getV(keys[k]).equals(ks[k])) {
							flag = false;
							break;
						}
					}
					if (flag) {
						bset.remove(bset.get(j));
					} else {
						j++;
					}
				}
			}
			DAOSet<T> set = new DAOSet<T>();
			Class<T> daoClass = (Class<T>) bset.get(0).getDAO().getClass();
			for (int j = 0; j < bset.size(); j++) {
				T dao = daoClass.newInstance();
				BackupDAO<T> bDAO = bset.get(j);
				for (int i = 0; i < dao.columns().length; i++) {
					dao.setV(i, bDAO.getV(i));
				}
				set.add(dao);
			}
			return set;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将一个DAO中的每个字段的数据拷贝到另一个DAO的同名字段，如果没有同名字段，则不拷贝<br>
	 * 注意：是深层拷贝，不仅仅是拷贝引用
	 */
	public static boolean copyFieldValue(DAO<?> srcDAO, DAO<?> destDAO) {
		try {
			DAOColumn[] srcSC = srcDAO.columns();
			DAOColumn[] destSC = destDAO.columns();
			for (int i = 0; i < srcSC.length; i++) {
				for (int j = 0; j < destSC.length; j++) {
					if (srcSC[i].getColumnName().equals(destSC[j].getColumnName())) {
						int order = j;
						Object v = srcDAO.getV(i);
						if (v == null) {
							destDAO.setV(order, null);
						} else if (v instanceof Date) {
							destDAO.setV(order, ((Date) v).clone());
						} else if (v instanceof Double) {
							destDAO.setV(order, new Double(((Double) v).doubleValue()));
						} else if (v instanceof Float) {
							destDAO.setV(order, new Float(((Float) v).floatValue()));
						} else if (v instanceof Integer) {
							destDAO.setV(order, new Integer(((Integer) v).intValue()));
						} else if (v instanceof Long) {
							destDAO.setV(order, new Long(((Long) v).longValue()));
						} else if (v instanceof byte[]) {
							destDAO.setV(order, ((byte[]) v).clone());
						} else if (v instanceof String) {
							destDAO.setV(order, v);
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 获得唯一备份号
	 */
	public static String getBackupNo() {
		return String.valueOf(BackupNoBase.incrementAndGet()).substring(1);
	}

	/**
	 * 为PreparedStatement设置参数
	 */
	public static void setParam(DAO<?> dao, DAOColumn sc, PreparedStatement pstmt, DBConn conn, int i, Object v) throws SQLException {
		IDBType db = DBTypeService.getInstance().get(conn.getDBConfig().DBType);
		if (v == null) {
			if (sc.getColumnType() == DataTypes.LONG) {
				pstmt.setNull(i + 1, java.sql.Types.BIGINT);
			} else if (sc.getColumnType() == DataTypes.INTEGER) {
				pstmt.setNull(i + 1, java.sql.Types.INTEGER);
			} else if (sc.getColumnType() == DataTypes.CLOB) {
				if (conn.getDBConfig().isSybase()) {
					db.setClob(conn, pstmt, i + 1, "");// Sybase不允许text类型为空
				} else {
					pstmt.setNull(i + 1, java.sql.Types.CLOB);
				}
			} else if (sc.getColumnType() == DataTypes.BLOB) {
				pstmt.setNull(i + 1, java.sql.Types.BLOB);
			} else if (sc.getColumnType() == DataTypes.DOUBLE) {
				pstmt.setNull(i + 1, java.sql.Types.DOUBLE);
			} else if (sc.getColumnType() == DataTypes.FLOAT) {
				pstmt.setNull(i + 1, java.sql.Types.FLOAT);
			} else if (sc.getColumnType() == DataTypes.DECIMAL) {
				pstmt.setNull(i + 1, java.sql.Types.DECIMAL);
			} else if (sc.getColumnType() == DataTypes.DATETIME) {
				pstmt.setNull(i + 1, java.sql.Types.DATE);
			} else if (sc.getColumnType() == DataTypes.BIT) {
				pstmt.setNull(i + 1, java.sql.Types.BIT);
			} else if (sc.getColumnType() == DataTypes.SMALLINT) {
				pstmt.setNull(i + 1, java.sql.Types.SMALLINT);
			} else {
				pstmt.setNull(i + 1, java.sql.Types.VARCHAR);
			}
		} else {
			if (sc.getColumnType() == DataTypes.DATETIME) {
				pstmt.setTimestamp(i + 1, new java.sql.Timestamp(((java.util.Date) v).getTime()));
			} else if (sc.getColumnType() == DataTypes.CLOB) {
				String str = (String) v;
				if (conn.getDBConfig().isLatin1Charset && conn.getDBConfig().isOracle()) {// Oracle必须特别处理
					try {
						str = new String(str.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				db.setClob(conn, pstmt, i + 1, str);
			} else if (sc.getColumnType() == DataTypes.BLOB) {
				db.setBlob(conn, pstmt, i + 1, (byte[]) v);
			} else if (sc.getColumnType() == DataTypes.STRING) {
				String str = (String) v;
				int len = StringUtil.lengthEx(str, Config.getGlobalCharset().equals("UTF-8"));
				if (len > sc.getLength()) {
					throw new DAOException(dao.table() + "." + sc.getColumnName() + " is too long, max is " + sc.getLength()
							+ ",actual is " + len + ",value=" + str);
				}
				if (conn.getDBConfig().isLatin1Charset && conn.getDBConfig().isOracle()) {// Oracle必须特别处理
					try {
						str = new String(str.getBytes(Config.getGlobalCharset()), "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				if (conn.getDBConfig().isSybase() && str.equals("")) {
					pstmt.setNull(i + 1, java.sql.Types.VARCHAR);
				} else {
					pstmt.setString(i + 1, str);
				}
			} else {
				pstmt.setObject(i + 1, v);
			}
		}
	}

	/**
	 * 返回当前应用下的所有DAO类的名称
	 */
	public static String[] getAllDAOClassName() {
		DAOClassVisitor v = new DAOClassVisitor();
		BuiltResourceScanner scanner = new BuiltResourceScanner(v, null);
		scanner.scan(0);
		Set<String> list = v.getResult();
		String[] arr = new String[list.size()];
		arr = list.toArray(arr);
		return arr;
	}

	/**
	 * 获得表代码，主要是提供给框架中的其他类使用，因为Schmea.TableCode被protected修饰
	 */
	public static String getTableCode(DAO<?> dao) {
		return dao.table();
	}

	public static DAOColumn[] getColumns(DAO<?> dao) {
		return dao.columns();
	}

	public static DAOColumn[] addBackupColumn(DAOColumn[] scs) {
		DAOColumn[] bscs = new DAOColumn[scs.length + 4];
		for (int i = 0; i < scs.length; i++) {
			bscs[i] = scs[i];
		}
		bscs[scs.length] = new DAOColumn("BackupNo", DataTypes.STRING, 15, 0, true, true);
		bscs[scs.length + 1] = new DAOColumn("BackupOperator", DataTypes.STRING, 50, 0, true, false);
		bscs[scs.length + 2] = new DAOColumn("BackupTime", DataTypes.DATETIME, 0, 0, true, false);
		bscs[scs.length + 3] = new DAOColumn("BackupMemo", DataTypes.STRING, 200, 0, false, false);
		return bscs;
	}

	public static class DAOClassVisitor implements IBuiltResourceVisitor {
		private static final String SUPER = DAO.class.getName().replace('.', '/');
		private static final String FRAMEWORK = "com/zving/framework";
		Set<String> set = new HashSet<String>();

		@Override
		public String getExtendItemID() {
			return "DAOClasVisitor";
		}

		@Override
		public String getExtendItemName() {
			return "DAOClasVisitor";
		}

		@Override
		public boolean match(BuiltResource br) {
			if (br.getFullName().indexOf(FRAMEWORK) >= 0) {
				return false;
			}
			return true;
		}

		@Override
		public void visitClass(BuiltResource br, ClassNode cn) {
			if (cn.superName.equals(SUPER) && (cn.access & Opcodes.ACC_ABSTRACT) == 0) {
				set.add(cn.name.replace('/', '.'));
			}
		}

		@Override
		public void visitInnerClass(BuiltResource br, ClassNode outerClass, ClassNode innerClass) {
		}

		@Override
		public void visitResource(BuiltResource br) {
		}

		public Set<String> getResult() {
			return set;
		}
	}
}
