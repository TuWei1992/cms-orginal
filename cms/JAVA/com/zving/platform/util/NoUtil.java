package com.zving.platform.util;

import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DBConnPoolManager;
import com.zving.framework.data.DataAccess;
import com.zving.framework.data.Q;
import com.zving.framework.data.dbtype.DBTypeService;
import com.zving.framework.data.dbtype.MSSQL;
import com.zving.framework.data.dbtype.MSSQL2000;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZDMaxNo;

/**
 * 最大号工具类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2006-10-5
 */
public class NoUtil {
	@Deprecated
	public static long getMaxID(String noType, String subType) {
		return getMaxID(noType, subType, 1);
	}

	/**
	 * 根据数据库类型不同构建SQL不同
	 * 
	 * @param noType
	 * @param subType
	 * @return
	 */
	private static Q getMaxIDQ(String noType, String subType) {
		DBConnConfig dbcc = DBConnPoolManager.getDBConnConfig(DBConnPoolManager.DEFAULT_POOLNAME);
		String forUpdate = DBTypeService.getInstance().get(dbcc.DBType).getForUpdate();
		Q q = new Q("select NoMaxValue from ZDMaxNo");
		if (MSSQL.ID.equals(dbcc.DBType) || MSSQL2000.ID.equals(dbcc.DBType)) {
			q.append(forUpdate);
			q.append(" where NoType=? and NoSubType=?", noType, subType);
		} else {
			q.append(" where NoType=? and NoSubType=?", noType, subType);
			q.append(forUpdate);
		}
		return q;
	}

	/**
	 * @param noType 类型
	 * @param subType 子类型
	 * @param size 一次申请的ID数
	 * @return
	 */
	private static long getMaxID(String noType, String subType, int size) {
		if (size < 1) {
			size = 1;
		}
		DBConn conn = DBConnPoolManager.getConnection(DBConnPoolManager.DEFAULT_POOLNAME, false, false);// 不能使用阻塞型事务中的连接
		DataAccess da = new DataAccess(conn);
		try {
			da.setAutoCommit(false);
			da.nonuseRWSpliting();
			Q q = getMaxIDQ(noType, subType);
			Object maxValue = da.executeOneValue(q);
			if (maxValue != null) {
				long t = Long.parseLong(maxValue.toString()) + size;
				q = new Q("update ZDMaxNo set NoMaxValue=? where NoType=? and NoSubType=?", t, noType, subType);
				da.executeNoQuery(q);
				da.commit();
				return t;
			} else {
				ZDMaxNo maxno = new ZDMaxNo();
				maxno.setNoType(noType);
				maxno.setNoSubType(subType);
				maxno.setNoMaxValue(size);
				maxno.setLength(10);
				maxno.setDataAccess(da);
				if (maxno.insert()) {
					da.commit();
					return size;
				} else {
					throw new RuntimeException("获取最大号时发生错误!");
				}
			}
		} catch (Exception e) {
			da.rollback();
			throw new RuntimeException("获取最大号时发生错误:" + e.getMessage());
		} finally {
			try {
				da.setAutoCommit(true);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			da.close();
		}
	}

	/**
	 * 得到类型为noType位长为length的编码
	 */
	public static String getMaxNo(String noType, int length) {
		long t = getMaxID(noType, "SN", 1);
		String no = String.valueOf(t);
		if (no.length() > length) {
			return no.substring(0, length);
		}
		return StringUtil.leftPad(no, '0', length);
	}

	/**
	 * 得到类型为noType，位长为length且前缀为prefix的编码
	 */
	public static String getMaxNo(String noType, String prefix, int length) {
		long t = getMaxID(noType, prefix, 1);
		String no = String.valueOf(t);
		if (no.length() > length) {
			return no.substring(0, length);
		}
		return prefix + StringUtil.leftPad(no, '0', length);
	}

	public static long getMaxID(String noType) {
		return getMaxID(noType, "ID", 1);
	}

	private static ConcurrentMapx<String, long[]> idMap = new ConcurrentMapx<String, long[]>();

	/**
	 * 批量获取最大ID。每次调用先占用size个ID并缓存，然后从缓存中取ID，取完后再次获取size个ID，直到程序结束。 <br>
	 * 本方法特别适用于批量导入数据的场合。
	 */
	public synchronized static long getMaxID(String noType, int size) {
		if (size < 1) {
			return getMaxID(noType, "ID", 1);
		}
		long[] p = idMap.get(noType);
		if (p == null) {
			p = new long[2];
			idMap.put(noType, p);
		}
		p[0] = p[0] + 1L;
		if (p[0] > p[1]) {
			p[1] = getMaxID(noType, "ID", size);
			p[0] = p[1] - size + 1;
		}
		return p[0];
	}
}
