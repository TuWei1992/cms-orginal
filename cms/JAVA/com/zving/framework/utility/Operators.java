package com.zving.framework.utility;

import java.util.Date;

/**
 * 操作符对应的操作逻辑
 * 
 * @Author 王育春
 * @Date 2010-12-9
 * @Mail wyuch@zving.com
 */
public class Operators {// NO_UCD
	/**
	 * 加
	 */
	public static Object plus(Object obj1, Object obj2) {
		if (!(obj1 instanceof Number)) {
			return obj1.toString() + obj2;
		}
		double d1 = Primitives.getDouble(obj1);
		double d2 = Primitives.getDouble(obj2);
		if (obj1 instanceof Integer || obj1 instanceof Long) {
			if (obj2 instanceof Integer || obj2 instanceof Long) {
				return new Double(d1 + d2).longValue();
			} else if (obj2 instanceof String && NumberUtil.isLong(obj2.toString())) {
				return new Double(d1 + d2).longValue();
			}
		}
		return new Double(d1 + d2);
	}

	/**
	 * 减
	 */
	public static Object minus(Object obj1, Object obj2) {
		double d1 = Primitives.getDouble(obj1);
		double d2 = Primitives.getDouble(obj2);
		if (obj1 instanceof Integer || obj1 instanceof Long) {
			if (obj2 instanceof Integer || obj2 instanceof Long) {
				return new Double(d1 - d2).longValue();
			} else if (obj2 instanceof String && NumberUtil.isLong(obj2.toString())) {
				return new Double(d1 - d2).longValue();
			}
		} else if (obj1 instanceof String && NumberUtil.isLong(obj1.toString())) {
			if (obj2 instanceof Integer || obj2 instanceof Long) {
				return new Double(d1 - d2).longValue();
			} else if (obj2 instanceof String && NumberUtil.isLong(obj2.toString())) {
				return new Double(d1 - d2).longValue();
			}
		}
		return new Double(d1 - d2);
	}

	/**
	 * 负
	 */
	public static Object minus(Object obj1) {
		double d1 = Primitives.getDouble(obj1);
		if (obj1 instanceof Integer || obj1 instanceof Long) {
			return new Double(-d1).longValue();
		}
		return new Double(-d1);
	}

	/**
	 * 乘
	 */
	public static Object multiply(Object obj1, Object obj2) {
		double d1 = Primitives.getDouble(obj1);
		double d2 = Primitives.getDouble(obj2);
		if (obj1 instanceof Integer || obj1 instanceof Long) {
			if (obj2 instanceof Integer || obj2 instanceof Long) {
				return new Double(d1 * d2).longValue();
			}
		}
		if (obj1 instanceof String && obj2 instanceof String) {
			if (NumberUtil.isLong(obj1.toString()) && NumberUtil.isLong(obj2.toString())) {
				return new Double(d1 * d2).longValue();
			}
		}
		return new Double(d1 * d2);
	}

	/**
	 * 除
	 */
	public static Object divide(Object obj1, Object obj2) {
		double d1 = Primitives.getDouble(obj1);
		double d2 = Primitives.getDouble(obj2);
		if (d2 == 0) {
			return new Double(0);
		}
		if (obj1 instanceof Integer || obj1 instanceof Long) {
			if (obj2 instanceof Integer || obj2 instanceof Long) {
				return new Double(d1 / d2).longValue();
			}
		}
		if (obj1 instanceof String && obj2 instanceof String) {
			if (NumberUtil.isLong(obj1.toString()) && NumberUtil.isLong(obj2.toString())) {
				return new Double(d1 / d2).longValue();
			}
		}
		return new Double(d1 / d2);
	}

	/**
	 * 求余
	 */
	public static Object mod(Object obj1, Object obj2) {
		long d1 = Primitives.getLong(obj1);
		long d2 = Primitives.getLong(obj2);
		if (d2 == 0) {
			return new Double(0);
		}
		return new Long(d1 % d2);
	}

	/**
	 * 等于
	 */
	public static Object eq(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return Boolean.TRUE;
		}
		if (obj1 == null && obj2 != null) {
			if (obj2.equals("")) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		if (obj2 == null && obj1 != null) {
			if (obj1.equals("")) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		if (obj1 instanceof Number || obj2 instanceof Number) {
			return Primitives.getBoolean(Primitives.getDouble(obj1) == Primitives.getDouble(obj2));
		}
		if (obj1 instanceof Date || obj2 instanceof Date) {
			if (obj1 instanceof String) {
				if (DateUtil.isDateTime(obj1.toString())) {
					return Primitives.getBoolean(DateUtil.parseDateTime(obj1.toString()).equals(obj2));
				}
				return Boolean.FALSE;
			} else if (obj2 instanceof String) {
				if (DateUtil.isDateTime(obj2.toString())) {
					return Primitives.getBoolean(DateUtil.parseDateTime(obj2.toString()).equals(obj1));
				}
				return Boolean.FALSE;
			} else {
				return Primitives.getBoolean(obj1.equals(obj2));
			}
		}
		if (obj1 instanceof Boolean && obj1.toString().equals(obj2)) {
			return Boolean.TRUE;
		}
		if (obj2 instanceof Boolean && obj2.toString().equals(obj1)) {
			return Boolean.TRUE;
		}
		return Primitives.getBoolean(obj1.equals(obj2));
	}

	/**
	 * 不等于
	 */
	public static Object ne(Object obj1, Object obj2) {
		Boolean b = (Boolean) eq(obj1, obj2);
		return Primitives.getBoolean(!b.booleanValue());
	}

	/**
	 * 大于
	 */
	@SuppressWarnings("unchecked")
	public static Object gt(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return Boolean.FALSE;
		}
		if (obj1 == null) {
			return Boolean.FALSE;
		}
		if (obj2 == null) {
			return Boolean.TRUE;
		}
		if (obj1 instanceof Number || obj2 instanceof Number) {// 数值比较
			return Primitives.getBoolean(Primitives.getDouble(obj1) > Primitives.getDouble(obj2));
		} else if (obj1 instanceof Date || obj2 instanceof Date) {// 日期比校
			if (obj1 instanceof Date) {
				if (obj2 instanceof Date) {
					return Primitives.getBoolean(((Date) obj1).getTime() > ((Date) obj2).getTime());
				} else {
					Date d2 = DateUtil.parseDateTime(obj2.toString());
					return Primitives.getBoolean(((Date) obj1).getTime() > d2.getTime());
				}
			} else {
				Date d1 = DateUtil.parseDateTime(obj1.toString());
				return Primitives.getBoolean(d1.getTime() > ((Date) obj2).getTime());
			}
		} else if (DateUtil.isDateTime(obj1.toString()) && DateUtil.isDateTime(obj2.toString())) {
			Date d1 = DateUtil.parseDateTime(obj1.toString());
			Date d2 = DateUtil.parseDateTime(obj2.toString());
			return Primitives.getBoolean(d1.getTime() > d2.getTime());
		} else if (obj1 instanceof Comparable && obj2 instanceof Comparable) {
			return Primitives.getBoolean(((Comparable<Object>) obj1).compareTo(obj2) > 0);
		} else {
			try {
				double b1 = Double.parseDouble(obj1.toString());
				double b2 = Double.parseDouble(obj2.toString());
				return Primitives.getBoolean(b1 > b2);
			} catch (Exception e) {
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * 大于等于
	 */
	public static Object ge(Object obj1, Object obj2) {
		Boolean b = (Boolean) gt(obj1, obj2);
		if (b.booleanValue()) {
			return b;
		}
		return eq(obj1, obj2);
	}

	/**
	 * 小于
	 */
	@SuppressWarnings("unchecked")
	public static Object lt(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return Boolean.FALSE;
		}
		if (obj1 == null) {
			return Boolean.TRUE;
		}
		if (obj2 == null) {
			return Boolean.FALSE;
		}
		if (obj1 instanceof Number || obj2 instanceof Number) {// 数值比较
			return Primitives.getBoolean(Primitives.getDouble(obj1) < Primitives.getDouble(obj2));
		} else if (obj1 instanceof Date || obj2 instanceof Date) {// 日期比校
			if (obj1 instanceof Date) {
				if (obj2 instanceof Date) {
					return Primitives.getBoolean(((Date) obj1).getTime() < ((Date) obj2).getTime());
				} else {
					Date d2 = DateUtil.parseDateTime(obj2.toString());
					return Primitives.getBoolean(((Date) obj1).getTime() < d2.getTime());
				}
			} else {
				Date d1 = DateUtil.parseDateTime(obj1.toString());
				return Primitives.getBoolean(d1.getTime() < ((Date) obj2).getTime());
			}
		} else if (DateUtil.isDateTime(obj1.toString()) && DateUtil.isDateTime(obj2.toString())) {
			Date d1 = DateUtil.parseDateTime(obj1.toString());
			Date d2 = DateUtil.parseDateTime(obj2.toString());
			return Primitives.getBoolean(d1.getTime() < d2.getTime());
		} else if (obj1 instanceof Comparable && obj2 instanceof Comparable) {
			return Primitives.getBoolean(((Comparable<Object>) obj1).compareTo(obj2) < 0);
		} else {
			try {
				double b1 = Double.parseDouble(obj1.toString());
				double b2 = Double.parseDouble(obj2.toString());
				return Primitives.getBoolean(b1 < b2);
			} catch (Exception e) {
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * 小于等于
	 */
	public static Object le(Object obj1, Object obj2) {
		Boolean b = (Boolean) lt(obj1, obj2);
		if (b.booleanValue()) {
			return b;
		}
		return eq(obj1, obj2);
	}

	/**
	 * 逻辑与
	 */
	public static Object and(Object obj1, Object obj2) {
		boolean b1 = Primitives.getBoolean(obj1);
		if (b1) {
			boolean b2 = Primitives.getBoolean(obj2);
			return Primitives.getBoolean(b1 && b2);
		} else {
			return Boolean.FALSE;
		}
	}

	/**
	 * 逻辑或
	 */
	public static Object or(Object obj1, Object obj2) {
		boolean b1 = Primitives.getBoolean(obj1);
		if (!b1) {
			boolean b2 = Primitives.getBoolean(obj2);
			return Primitives.getBoolean(b1 || b2);
		} else {
			return Boolean.TRUE;
		}
	}

	/**
	 * 逻辑非
	 */
	public static Object not(Object obj1) {
		boolean b1 = Primitives.getBoolean(obj1);
		return Primitives.getBoolean(!b1);
	}
}
