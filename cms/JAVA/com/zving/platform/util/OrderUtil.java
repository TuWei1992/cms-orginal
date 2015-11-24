package com.zving.platform.util;

import java.util.Arrays;

import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.orm.DAO;
import com.zving.framework.thirdparty.commons.ArrayUtils;
import com.zving.framework.utility.Primitives;
import com.zving.framework.utility.StringUtil;

/**
 * 排序工具类
 * 
 * @Author 王育春
 * @Date 2009-2-27
 * @Mail wyuch@zving.com
 */
public class OrderUtil {
	public static final String AFTER = "After";
	private static long currentOrder = System.currentTimeMillis();

	/**
	 * 使用指定字段排序(自然数递增),将指定的DAO移动到目标位置,并更新受影响行的排序序号
	 * 
	 * @param table 排序的表
	 * @param column 排序字段名(long类型,增长序列)
	 * @param targetOrderFlag 目标位置
	 * @param wherePart and开头的条件sql语句
	 * @param sortDAO 移动的DAO
	 * @param tran
	 */
	public static void updateOrder(String table, String column, long targetOrderFlag, Q wherePart, DAO<?> sortDAO, Transaction tran) {
		long orderFlag = Primitives.getLong(sortDAO.getV(column));
		if (orderFlag > targetOrderFlag) {
			Q q = new Q().update(table).set().self(column, "+", 1).where().ge(column, targetOrderFlag).and().lt(column, orderFlag);
			q.addPart(wherePart);
			tran.add(q);
		} else {
			Q q = new Q().update(table).set().self(column, "-", 1).where().gt(column, orderFlag).and().le(column, targetOrderFlag);
			q.addPart(wherePart);
			tran.add(q);
		}
		sortDAO.setV(column, targetOrderFlag);
		tran.update(sortDAO);
	}

	/**
	 * 刷新排序序列,按照自然数从小到大重新设置排序字段大小(注意:不适用于大量数据操作)
	 * 
	 * @param table
	 * @param pkColumn
	 * @param orderColumn
	 * @param wherePart
	 */
	public static void refreshSort(String table, String pkColumn, String orderColumn, Q wherePart, Transaction tran) {
		long orderSort = 0;
		Q q = new Q().select(pkColumn, orderColumn).from(table);
		if (wherePart != null) {
			q.where().append(" 1=1 ").addPart(wherePart);
		}
		q.orderby(orderColumn).append(" asc");
		DataTable dt = q.fetch();
		if (dt == null || dt.getRowCount() == 0) {
			return;
		}
		for (DataRow dataRow : dt) {
			tran.add(new Q().update(table).set(orderColumn, orderSort++).where(pkColumn, dataRow.get(0)));
		}
	}

	/**
	 * 使用OrderFlag(自然数递增)字段排序,将指定的DAO移动到目标位置,并更新受影响行的排序序号
	 * 
	 * @param table 排序的表
	 * @param targetOrderFlag 目标位置
	 * @param wherePart and开头的条件sql语句
	 * @param sortDAO 移动的DAO
	 * @param tran
	 */
	public static void updateOrder(String table, long targetOrderFlag, Q wherePart, DAO<?> sortDAO, Transaction tran) {
		updateOrder(table, "OrderFlag", targetOrderFlag, wherePart, sortDAO, tran);
	}

	/**
	 * 使用OrderFlag(自然数递增)字段排序,将指定的DAO移动到目标位置,并更新受影响行的排序序号
	 * 
	 * @param table 排序的表
	 * @param targetOrderFlag 目标位置
	 * @param sortDAO 移动的DAO
	 * @param tran
	 */
	public static void updateOrder(String table, long targetOrderFlag, DAO<?> sortDAO, Transaction tran) {
		updateOrder(table, "OrderFlag", targetOrderFlag, null, sortDAO, tran);
	}

	/**
	 * 使用指定字段(时间戳递减)排序，将多条数据移动到指定位置
	 * 
	 * @param table 排序的表
	 * @param column 指定的排序列((long类型,时间戳递减型))
	 * @param type 移动到目标位置前或后(not "After")||"After"
	 * @param targetOrder 目标位置
	 * @param orders 要移动的数据当前位置(多条使用','分隔)
	 * @param wherePart and开头的条件sql语句
	 * @param tran 如果该参数为null则会新建事务并在该方法内提交，并返回事物提交的结果
	 * @return
	 */
	public static boolean updateOrder(String table, String column, String type, String targetOrder, String orders, Q wherePart,
			Transaction tran) {
		if (StringUtil.isEmpty(targetOrder) || targetOrder.length() < 13) {// 拖到最前时会有这种现象
			targetOrder = "" + getDefaultOrder();
		}
		if (!StringUtil.checkID(targetOrder)) {
			return false;
		}
		if (!StringUtil.checkID(orders)) {
			return false;
		}
		if (wherePart == null) {
			wherePart = new Q();
		}

		String[] arrtmp = orders.split(",");
		arrtmp = ArrayUtils.removeElement(arrtmp, targetOrder);
		long[] arr = new long[arrtmp.length + 1];
		for (int i = 0; i < arrtmp.length; i++) {
			arr[i] = Long.parseLong(arrtmp[i]);
		}
		long target = Long.parseLong(targetOrder);
		arr[arrtmp.length] = target;
		Arrays.sort(arr);

		boolean bFlag = true;// 有连接传入
		if (tran == null) {
			tran = new Transaction();
			bFlag = false;
		}
		Q q = null;
		boolean flag = AFTER.equals(type);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == target) {
				if (flag) {
					target = target + arr.length - i - 1;
					int d = arr.length - 1;
					for (int j = 0; j < arr.length; j++) {
						if (j != i) {
							q = new Q().update(table).set(column, (target - d) * 10L).where(column, arr[j]);
							d--;
						} else {
							q = new Q().update(table).set(column, target * 10L).where(column, arr[j]);
						}
						tran.add(q);
					}
					// 排序在target之后的有i个
					for (int j = 0; j < i; j++) {
						if (arr[j] + 1 == arr[j + 1]) {
							continue;
						}
						q = new Q().update(table).set().self(column, "-", j + 1).where().between(column, arr[j], arr[j + 1]);
						q.addPart(wherePart);
						tran.add(q);
					}

					// 之前的有arr.length-i-1个
					for (int j = arr.length - 1; j > i; j--) {
						if (arr[j] == arr[j - 1] + 1) {
							continue;
						}
						q = new Q().update(table).set().self(column, "+", arr.length - j).where().between(column, arr[j - 1], arr[j]);
						q.addPart(wherePart);
						tran.add(q);
					}
				} else {
					target = target - i;
					int d = 1;
					for (int j = 0; j < arr.length; j++) {
						if (j != i) {
							q = new Q().update(table).set(column, (target + d) * 10L).where(column, arr[j]);
							d++;
						} else {
							q = new Q().update(table).set(column, target * 10L).where(column, arr[j]);
						}
						tran.add(q);
					}

					// 排序在target之后的有i个
					for (int j = 0; j < i; j++) {
						if (arr[j] + 1 == arr[j + 1]) {
							continue;
						}
						q = new Q().update(table).set().self(column, "-", j + 1).where().between(column, arr[j], arr[j + 1]);
						q.addPart(wherePart);
						tran.add(q);
					}

					// 之前的有arr.length-i-1个
					for (int j = arr.length - 1; j > i; j--) {
						if (arr[j] == arr[j - 1] + 1) {
							continue;
						}
						q = new Q().update(table).set().self(column, "+", arr.length - j).where().between(column, arr[j - 1], arr[j]);
						q.addPart(wherePart);
						tran.add(q);
					}
				}
				q = new Q().update(table).set().self(column, "/", 10).where().gt(column, target * 9);
				q.and().lt(column, target * 18);
				q.addPart(wherePart);
				tran.add(q);
				if (bFlag) {
					return true;
				} else {
					return tran.commit();
				}
			}
		}
		return false;
	}

	public static synchronized long getDefaultOrder() {
		if (System.currentTimeMillis() <= currentOrder) {
			++currentOrder;
		} else {
			currentOrder = System.currentTimeMillis();
		}
		return currentOrder * 100;
	}
}
