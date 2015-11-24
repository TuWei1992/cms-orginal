package com.zving.framework.utility;

import com.zving.framework.core.castor.BooleanCastor;
import com.zving.framework.core.castor.DoubleCastor;
import com.zving.framework.core.castor.FloatCastor;
import com.zving.framework.core.castor.IntCastor;
import com.zving.framework.core.castor.LongCastor;

/**
 * 基本类型工具类
 * 
 * @Author 王育春
 * @Date 2010-12-10
 * @Mail wyuch@zving.com
 */
public class Primitives {
	public static final Class<?>[] primitiveClass = new Class<?>[] { int.class, long.class, boolean.class, char.class, byte.class,
			short.class, Integer.class, Long.class, Boolean.class, Character.class, Byte.class, Short.class };
	public static final Class<?>[] primitiveArrClass = new Class<?>[] { int[].class, long[].class, boolean[].class, char[].class,
			byte.class, short.class, Integer[].class, Long[].class, Boolean[].class, Character[].class, Byte[].class, Short[].class };

	/**
	 * 指定的对象是否是基本类型
	 */
	public static boolean isPrimitives(Object obj) {
		return ObjectUtil.in(obj.getClass(), primitiveClass);
	}

	/**
	 * 指定的对象是否是基本类型数组
	 */
	public static boolean isPrimitiveArray(Object obj) {
		return ObjectUtil.in(obj.getClass(), primitiveArrClass);
	}

	/**
	 * 将对象转换成double,如果不能转换则返回0
	 */
	public static double getDouble(Object obj) {
		return ((Number) DoubleCastor.getInstance().cast(obj, Double.class)).doubleValue();
	}

	/**
	 * 将对象转换成float,如果不能转换则返回0
	 */
	public static float getFloat(Object obj) {
		return ((Number) FloatCastor.getInstance().cast(obj, Float.class)).floatValue();
	}

	/**
	 * 将对象转换成long,如果不能转换则返回0
	 */
	public static long getLong(Object obj) {
		return ((Number) LongCastor.getInstance().cast(obj, Long.class)).longValue();

	}

	/**
	 * 将对象转换成int,如果不能转换则返回0
	 */
	public static int getInteger(Object obj) {
		return ((Number) IntCastor.getInstance().cast(obj, Integer.class)).intValue();

	}

	/**
	 * 将boolean转换成对象类型Boolean
	 */
	public static Boolean getBoolean(boolean flag) {
		return flag ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 将对象转换成double,如果不能转换则返回false
	 */
	public static boolean getBoolean(Object obj) {
		return (Boolean) BooleanCastor.getInstance().cast(obj, Boolean.class);
	}
}
