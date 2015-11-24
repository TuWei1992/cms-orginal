package com.zving.framework.utility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zving.framework.collection.Filter;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;

/**
 * 对象工具类 <br>
 * 
 * @author 王育春<br>
 * @date 2006-7-12<br>
 * @email wangyc@zving.com<br>
 */
public class ObjectUtil {// NO_UCD
	/**
	 * 检查第一个参数是否等于后续参数中的一个。<br>
	 * 如第一个参数不是数组或者集合，但后续参数中有数组或者集合，则会取出其中的元素逐一比较
	 */
	public static boolean in(Object... args) {
		if (args == null || args.length < 2) {
			return false;
		}
		Object arg1 = args[0];
		for (int i = 1; i < args.length; i++) {
			if (arg1 == null) {
				if (args[i] == null) {
					return true;
				}
			} else {
				if (arg1.equals(args[i])) {
					return true;
				} else {
					// 如果后续参数是数组，则遍历数组并比较
					if (!arg1.getClass().isArray() && args[i] != null && args[i].getClass().isArray()) {
						for (Object obj : (Object[]) args[i]) {
							if (arg1.equals(obj)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean notIn(Object... args) {
		return !in(args);
	}

	/**
	 * 符合如下条件返回true： <br>
	 * 1、等于null<br>
	 * 2、equals("") <br>
	 * 3、java.lang.Number的子类且其值等于0
	 * 4、数组、Collection、Map、DataTable元素个数等于0
	 */
	public static boolean empty(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof String) {
			return obj.equals("");
		}
		if (obj instanceof Number) {
			return ((Number) obj).doubleValue() == 0;
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0;
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).size() == 0;
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).size() == 0;
		}
		if (obj instanceof DataTable) {
			return ((DataTable) obj).getRowCount() == 0;
		}
		return false;
	}

	/**
	 * 不等于null、0、空字符串时返回true
	 */
	public static boolean notEmpty(Object obj) {
		return !empty(obj);
	}

	/**
	 * 判断两个参数是否相等
	 */
	public static boolean equal(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		if (obj1 == null) {
			if (obj2 == null) {
				return true;
			} else {
				return false;
			}
		} else {
			return obj1.equals(obj2);
		}
	}

	/**
	 * 判断两个参数是否不相等
	 */
	public static boolean notEqual(Object obj1, Object obj2) {
		return !equal(obj1, obj2);
	}

	/**
	 * 获得传入的所有参数中值最小的
	 */
	public static Number minNumber(double... args) {
		if (args == null || args.length == 0) {
			return null;
		}
		Number minus = null;
		for (double t : args) {
			if (minus == null) {
				minus = t;
				continue;
			}
			if (minus.doubleValue() > t) {
				minus = t;
			}
		}
		return minus;
	}

	/**
	 * 获得传入的所有参数中值最大的
	 */
	public static Number maxNumber(double... args) {
		if (args == null || args.length == 0) {
			return null;
		}
		Number max = null;
		for (double t : args) {
			if (max == null) {
				max = t;
				continue;
			}
			if (max.doubleValue() < t) {
				max = t;
			}
		}
		return max;
	}

	/**
	 * 获得传入的所有参数中值最小的
	 */
	public static <T extends Comparable<T>> T min(T... args) {
		if (args == null || args.length == 0) {
			return null;
		}
		T minus = null;
		for (T t : args) {
			if (minus == null) {
				minus = t;
				continue;
			}
			if (t == null) {
				continue;
			}
			if (minus.compareTo(t) > 0) {
				minus = t;
			}
		}
		return minus;
	}

	/**
	 * 获得传入的所有参数中值最大的
	 */
	public static <T extends Comparable<T>> T max(T... args) {
		if (args == null || args.length == 0) {
			return null;
		}
		T max = null;
		for (T t : args) {
			if (max == null) {
				max = t;
				continue;
			}
			if (t == null) {
				continue;
			}
			if (max.compareTo(t) < 1) {
				max = t;
			}
		}
		return max;
	}

	/**
	 * 如果第一个参数不为空，则返回第一个参数，否则返回第二个参数
	 */
	public static <T> T ifEmpty(T obj1, T obj2) {
		return empty(obj1) ? obj2 : obj1;
	}

	/**
	 * 如果是null则返回null，否则返回Object.toString()
	 */
	public static String toString(Object obj) {
		return obj == null ? null : obj.toString();
	}

	/**
	 * 将参数组织成一个List
	 */
	public static <T> List<T> toList(T... args) {
		ArrayList<T> list = new ArrayList<T>(args.length);
		for (T t : args) {
			list.add(t);
		}
		return list;
	}

	/**
	 * 将参数组织成一个List
	 */
	public static <T> List<T> toList(Collection<T> c) {
		if (c instanceof List) {
			return (List<T>) c;
		}
		List<T> list = new ArrayList<T>(c.size());
		for (T t : c) {
			list.add(t);
		}
		return list;
	}

	public static List<String> toStringList(Collection<?> list) {
		List<String> r = new ArrayList<String>();
		for (Object obj : list) {
			r.add(obj == null ? null : obj.toString());
		}
		return r;
	}

	public static List<String> toStringList(Object[] arr) {
		List<String> r = new ArrayList<String>();
		for (Object obj : arr) {
			r.add(obj == null ? null : obj.toString());
		}
		return r;
	}

	public static Map<String, Object> toStringObjectMap(Map<?, ?> map) {
		HashMap<String, Object> r = new HashMap<String, Object>();
		for (Entry<?, ?> e : map.entrySet()) {
			r.put(e.getKey() == null ? null : e.getKey().toString(), e.getValue());
		}
		return r;
	}

	public static Map<String, String> toStringStringMap(Map<?, ?> map) {
		HashMap<String, String> r = new HashMap<String, String>();
		for (Entry<?, ?> e : map.entrySet()) {
			r.put(e.getKey() == null ? null : e.getKey().toString(), e.getValue() == null ? null : e.getValue().toString());
		}
		return r;
	}

	/**
	 * 返回过滤掉后的新数组，原数组保持不变。
	 */
	public static <T> T[] filter(T[] arr, Filter<T> filter) {
		if (arr == null || arr.length == 0) {
			return arr;
		}
		ArrayList<T> list = new ArrayList<T>();
		for (T t : arr) {
			if (filter.filter(t)) {
				list.add(t);
			}
		}
		@SuppressWarnings("unchecked")
		T[] a = (T[]) java.lang.reflect.Array.newInstance(arr.getClass().getComponentType(), list.size());
		int i = 0;
		for (T t : list) {
			a[i++] = t;
		}
		return a;
	}

	/**
	 * 返回过滤掉后的新List，原List保持不变。
	 */
	public static <T> List<T> filter(List<T> c, Filter<T> filter) {
		if (c == null || c.size() == 0) {
			return c;
		}
		List<T> list = new ArrayList<T>(c.size());
		for (T t : c) {
			if (filter.filter(t)) {
				list.add(t);
			}
		}
		return list;
	}

	/**
	 * 返回过滤掉后的新Mapx，原Mapx保持不变。
	 */
	public static <K, V> Map<K, V> filter(Map<K, V> map, Filter<Entry<K, V>> filter) {
		if (map == null || map.size() == 0) {
			return map;
		}
		try {
			@SuppressWarnings("unchecked")
			Map<K, V> map2 = map.getClass().newInstance();
			for (Entry<K, V> t : map.entrySet()) {
				if (filter.filter(t)) {
					map2.put(t.getKey(), t.getValue());
				}
			}
			return map2;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int[] toIntArray(Object[] arr) {
		int[] r = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			r[i] = Primitives.getInteger(arr[i]);
		}
		return r;
	}

	public static long[] toLongArray(Object[] arr) {
		long[] r = new long[arr.length];
		for (int i = 0; i < arr.length; i++) {
			r[i] = Primitives.getLong(arr[i]);
		}
		return r;
	}

	public static float[] toFloatArray(Object[] arr) {
		float[] r = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			r[i] = Primitives.getFloat(arr[i]);
		}
		return r;
	}

	public static double[] toDoubleArray(Object[] arr) {
		double[] r = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			r[i] = Primitives.getDouble(arr[i]);
		}
		return r;
	}

	public static boolean[] toBooleanArray(Object[] arr) {
		boolean[] r = new boolean[arr.length];
		for (int i = 0; i < arr.length; i++) {
			r[i] = Primitives.getBoolean(arr[i]);
		}
		return r;
	}

	public static String[] toStringArray(Collection<?> list) {
		String[] r = new String[list.size()];
		int i = 0;
		for (Object obj : list) {
			r[i++] = obj == null ? null : obj.toString();
		}
		return r;
	}

	public static String[] toStringArray(Object[] arr) {
		String[] r = new String[arr.length];
		for (int i = 0; i < arr.length; i++) {
			r[i] = arr[i] == null ? null : arr[i].toString();
		}
		return r;
	}

	public static <T> Object[] toObjectArray(T[] arr) {
		Object[] r = new Object[arr.length];
		for (int i = 0; i < arr.length; i++) {
			r[i] = arr[i];
		}
		return r;
	}

	/**
	 * <p>
	 * 将传入对象尝试转换为Object[]
	 * </p>
	 * <p>
	 * 注意：如果传入对象不是数组则抛出运行时异常
	 * </p>
	 * 
	 * @param arrayObject
	 * @return
	 */
	public static Object[] toObjectArray(Object arrayObject) {
		if (arrayObject == null) {
			return null;
		}
		if (!arrayObject.getClass().isArray()) {
			throw new RuntimeException("Not an array!");
		}
		if (arrayObject instanceof Object[]) {
			return (Object[]) arrayObject;
		}
		Object[] r = new Object[Array.getLength(arrayObject)];
		for (int i = 0; i < r.length; i++) {
			r[i] = Array.get(arrayObject, i);
		}
		return r;
	}

	/**
	 * 返回排序后的新数组，原数组保持不变。
	 */
	public static <T> T[] sort(T[] arr, Comparator<T> c) {
		if (arr == null || arr.length == 0) {
			return arr;
		}
		@SuppressWarnings("unchecked")
		T[] a = (T[]) java.lang.reflect.Array.newInstance(arr.getClass().getComponentType(), arr.length);
		int i = 0;
		for (T t : arr) {
			a[i++] = t;
		}
		Arrays.sort(a, c);
		return a;
	}

	/**
	 * 返回过排序后的新List，原List保持不变。
	 */
	public static <T> List<T> sort(List<T> arr, Comparator<T> c) {
		if (arr == null || arr.size() == 0) {
			return arr;
		}
		try {
			@SuppressWarnings("unchecked")
			T[] a = (T[]) java.lang.reflect.Array.newInstance(arr.toArray().getClass().getComponentType(), arr.size());
			a = arr.toArray(a);
			Arrays.sort(a, c);
			@SuppressWarnings("unchecked")
			List<T> list = arr.getClass().newInstance();
			for (T t : a) {
				list.add(t);
			}
			return list;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 返回排序掉后的新Mapx，原Mapx保持不变。
	 */
	public static <K, V> Mapx<K, V> sort(Mapx<K, V> map, Comparator<Entry<K, V>> c) {
		if (map == null || map.size() == 0) {
			return map;
		}
		try {
			@SuppressWarnings("unchecked")
			Entry<K, V>[] a = (Entry<K, V>[]) java.lang.reflect.Array.newInstance(Entry[].class.getComponentType(), map.size());
			@SuppressWarnings("unchecked")
			Mapx<K, V> map2 = map.getClass().newInstance();
			a = map.entrySet().toArray(a);
			Arrays.sort(a, c);
			for (Entry<K, V> t : a) {
				map2.put(t.getKey(), t.getValue());
			}
			return map2;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 字符串顺序排序器
	 */
	public static Comparator<String> ASCStringComparator = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			if (s1 == null) {
				if (s2 == null) {
					return 0;
				} else {
					return -1;
				}
			} else {
				if (s2 == null) {
					return 1;
				}
				return s1.compareTo(s2);
			}
		}
	};

	/**
	 * 字符串逆序排序器
	 */
	public static Comparator<String> DESCStringComparator = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return -ASCStringComparator.compare(o1, o2);
		}
	};

	public static String getCurrentStack() {
		return getStack(new Throwable());
	}

	public static String getStack(Throwable t) {
		StackTraceElement stack[] = t.getStackTrace();
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement ste : stack) {
			if (ste.getClassName().indexOf("ObjectUtil.getCurrentStack") == -1) {
				sb.append("\tat ");
				sb.append(ste.getClassName());
				sb.append(".");
				sb.append(ste.getMethodName());
				sb.append("(");
				sb.append(ste.getFileName());
				sb.append(":");
				sb.append(ste.getLineNumber());
				sb.append(")\n");
			}
		}
		return sb.toString();
	}

	/**
	 * 复制数组(从数组开始位置复制指定长度)
	 * 
	 * @param arr 要复制的数组
	 * @param length 复制的数组长度
	 * @return
	 */
	public static <T> T[] copyOf(T[] arr, int length) {
		@SuppressWarnings("unchecked")
		T[] copy = (T[]) java.lang.reflect.Array.newInstance(arr.getClass().getComponentType(), length);
		System.arraycopy(arr, 0, copy, 0, Math.min(arr.length, length));
		return copy;
	}

	/**
	 * 复制字节数组(从数组开始位置复制指定长度)
	 * 
	 * @param arr 要复制的数组
	 * @param length 复制的数组长度
	 * @return
	 */
	public static byte[] copyOf(byte[] arr, int length) {
		byte[] copy = new byte[length];
		System.arraycopy(arr, 0, copy, 0, Math.min(arr.length, length));
		return copy;
	}

	/**
	 * 复制布尔类型数组(从数组开始位置复制指定长度)
	 * 
	 * @param arr 要复制的数组
	 * @param length 复制的数组长度
	 * @return
	 */
	public static boolean[] copyOf(boolean[] arr, int length) {
		boolean[] copy = new boolean[length];
		System.arraycopy(arr, 0, copy, 0, Math.min(arr.length, length));
		return copy;
	}

	/**
	 * 复制浮点型数组(从数组开始位置复制指定长度)
	 * 
	 * @param arr 要复制的数组
	 * @param length 复制的数组长度
	 * @return
	 */
	public static float[] copyOf(float[] arr, int length) {
		float[] copy = new float[length];
		System.arraycopy(arr, 0, copy, 0, Math.min(arr.length, length));
		return copy;
	}

	/**
	 * 复制整型数组(从数组开始位置复制指定长度)
	 * 
	 * @param arr 要复制的数组
	 * @param length 复制的数组长度
	 * @return
	 */
	public static int[] copyOf(int[] arr, int length) {
		int[] copy = new int[length];
		System.arraycopy(arr, 0, copy, 0, Math.min(arr.length, length));
		return copy;
	}

	/**
	 * 判断字符串是否为"true"或"false"(区分大小写)
	 * 
	 * @param v
	 * @return
	 */
	public static boolean isBoolean(String v) {
		return "true".equals(v) || "false".equals(v);
	}

	/**
	 * 判断对象是否为Boolean.TRUE或其字符串表示等于"true"
	 */
	public static boolean isTrue(Object v) {
		if (v == null) {
			return false;
		}
		if (v instanceof Boolean) {
			return (Boolean) v;
		}
		return "true".equals(v.toString());
	}
}
