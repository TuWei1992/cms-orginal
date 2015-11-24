package com.zving.framework.core.bean;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zving.framework.collection.Mapx;
import com.zving.framework.core.exception.BeanInitException;
import com.zving.framework.utility.Primitives;
import com.zving.framework.utility.StringUtil;

/**
 * Bean工具类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class BeanUtil {
	/**
	 * 将值Map中的值按键逐个设置到Bean的属性上
	 * 
	 * @param bean Bean实例
	 * @param values 值Map
	 */
	public static void fill(Object bean, Map<?, ?> values) {
		fill(bean, values, null);
	}

	/**
	 * 将值Map中的值按键逐个设置到Bean的属性上，只设置键名以指定前缀开始的值
	 * 
	 * @param bean Bean实例
	 * @param values 值Map
	 * @param keyPrefix 键前缀
	 */
	public static void fill(Object bean, Map<?, ?> values, String keyPrefix) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (Object k : values.keySet()) {
			if (k != null) {
				String key = k.toString();
				if (keyPrefix != null && key.startsWith(keyPrefix)) {// 根据前缀重组map
					map.put(key.substring(keyPrefix.length()), values.get(k));
				}
			}
		}
		BeanDescription bim = BeanManager.getBeanDescription(bean.getClass());
		fillArrays(bean, map);
		fillInnerBeans(bean, map);
		for (Object k : map.keySet()) {
			if (k == null) {
				continue;
			}
			String key = k.toString();
			if (key.indexOf("[") > 0 || key.indexOf(".") > 0) {
				continue;
			}
			Object v = map.get(k);
			BeanProperty bip = bim.getProperty(key);
			if (bip != null) {
				bip.write(bean, v);
			}
		}
	}

	private static void fillArrays(Object bean, Map<String, Object> map) {
		BeanDescription bim = BeanManager.getBeanDescription(bean.getClass());
		HashMap<String, Integer> arraySizes = new HashMap<String, Integer>();
		for (Object k : map.keySet()) {
			if (k != null) {
				String key = k.toString();
				if (key.indexOf("[") > 0) {
					int i1 = key.indexOf("[");
					int i2 = key.indexOf("]");
					String prefix = key.substring(0, i1);
					int index = 0;
					try {
						index = Integer.parseInt(key.substring(i1 + 1, i2));
						if (arraySizes.get(prefix) == null) {
							arraySizes.put(prefix, index);
						} else {
							if (arraySizes.get(prefix) < index) {
								arraySizes.put(prefix, index);
							}
						}
					} catch (Exception e) {
						continue;
					}
				}
			}
		}
		for (Object k : arraySizes.keySet().toArray()) {
			String prefix = k.toString();
			BeanProperty bip = bim.getProperty(prefix);
			if (bip == null || !bip.getPropertyType().isArray()) {
				arraySizes.remove(k);
				continue;
			}
			Object[] arr = (Object[]) createArray(bip.getPropertyType(), arraySizes.get(prefix) + 1);
			for (int i = 0; i < arr.length; i++) {
				arr[i] = create(bip.getPropertyType().getComponentType());
				fill(arr[i], map, prefix + "[" + i + "].");
			}
			bip.write(bean, arr);
		}
	}

	private static void fillInnerBeans(Object bean, Map<String, Object> map) {
		BeanDescription bim = BeanManager.getBeanDescription(bean.getClass());
		HashMap<String, String> innerBeans = new HashMap<String, String>();
		for (Object k : map.keySet()) {
			if (k != null) {
				String key = k.toString();
				if (key.indexOf("[") > 0) {
					continue;
				}
				int i = key.indexOf(".");
				if (i > 0) {
					innerBeans.put(key.substring(0, i), "");
				}
			}
		}
		for (String name : innerBeans.keySet()) {
			BeanProperty bip = bim.getProperty(name);
			if (bip != null) {
				Object v = bip.read(bean);
				if (v == null) {
					v = create(bip.getPropertyType());
					bip.write(bean, v);
				}
				if (!(v instanceof Date)) {
					fill(v, map, name + ".");
				}
			}
		}
	}

	/**
	 * 使用默认构造器创建Bean实例
	 * 
	 * @param type Class
	 * @return Class的实例
	 */
	public static Object create(Class<?> type) {
		try {
			if (Modifier.isAbstract(type.getModifiers())) {
				throw new BeanInitException("Bean class " + type.getName() + " is a abstract class!");
			}
			return type.newInstance();
		} catch (Exception e) {
			throw new BeanInitException("Bean class " + type.getName() + " has not default constructor!");
		}
	}

	/**
	 * 使用指定参数类型的构造器创建Bean实例
	 * 
	 * @param type Bean类型
	 * @param paramTypes 构造器参数类型
	 */
	public static Object create(Class<?> type, Class<?> paramTypes) {// NO_UCD
		try {
			if (Modifier.isAbstract(type.getModifiers())) {
				throw new BeanInitException("Bean class " + type.getName() + " is a abstract class!");
			}
			Constructor<?> c = type.getConstructor(paramTypes);
			if (c == null) {
				throw new BeanInitException("No constructor with parameters [" + StringUtil.join(paramTypes) + "] in Bean class "
						+ type.getName() + " n!");
			}
			return c.newInstance();
		} catch (Exception e) {
			throw new BeanInitException("Bean class " + type.getName() + " has not default constructor!");
		}
	}

	/**
	 * 创建指定类型指定长度的数组
	 * 
	 * @param type 数组元素类型
	 * @param length 数组长度
	 * @return 创建好的数组
	 */
	public static Object createArray(Class<?> type, int length) {
		try {
			if (!type.isArray()) {
				return null;
			}
			if (Modifier.isAbstract(type.getComponentType().getModifiers())) {
				throw new BeanInitException("Bean class " + type.getName() + " is a abstract class!");
			}
			return Array.newInstance(type.getComponentType(), length);
		} catch (Exception e) {
			throw new BeanInitException("Bean class " + type.getName() + " has not default constructor!");
		}
	}

	/**
	 * 将各个源Bean实例中的各个属性的值逐个复制到目标Bean实例中的属性上。
	 * 目标Bean中没有但源Bean中有的属性直接忽略。
	 * 
	 * @param srcBean 源Bean实例
	 * @param targetBean 目标Bean实例
	 */
	public static void copyProperties(Object srcBean, Object targetBean) {// NO_UCD
		BeanDescription m1 = BeanManager.getBeanDescription(srcBean.getClass());
		BeanDescription m2 = BeanManager.getBeanDescription(targetBean.getClass());
		for (Entry<String, BeanProperty> e1 : m1.getPropertyMap().entrySet()) {
			for (Entry<String, BeanProperty> e2 : m2.getPropertyMap().entrySet()) {
				if (e1.getKey().equals(e2.getKey())) {
					e2.getValue().write(targetBean, e1.getValue().read(srcBean));
					break;
				}
			}
		}
	}

	/**
	 * @param bean Bean实例
	 * @param name 属性名称
	 * @return 指定Bean实例中的指定属性的值
	 */
	public static Object get(Object bean, String name) {// NO_UCD
		BeanDescription bim = BeanManager.getBeanDescription(bean.getClass());
		return bim.getProperty(name).read(bean);
	}

	/**
	 * @param bean Bean实例
	 * @param name 属性名称
	 * @param value 属性的值
	 */
	public static void set(Object bean, String name, Object value) {
		BeanDescription bim = BeanManager.getBeanDescription(bean.getClass());
		bim.getProperty(name).write(bean, value);
	}

	/**
	 * 将Bean转为Map
	 * 
	 * @param bean Bean实例
	 */
	public static Mapx<String, Object> toMap(Object bean) {// NO_UCD
		return toMap(bean, false, new HashMap<Object, Object>());
	}

	/**
	 * 将Bean转为Map
	 * 
	 * @param bean Bean实例
	 * @param pairFlag 是否只转对称的属性（即canRead()和canWrite()同时为true）
	 */
	public static Mapx<String, Object> toMap(Object bean, boolean pairFlag) {
		return toMap(bean, pairFlag, new HashMap<Object, Object>());
	}

	private static Mapx<String, Object> toMap(Object bean, boolean pairFlag, HashMap<Object, Object> convertedMap) {
		BeanDescription bim = BeanManager.getBeanDescription(bean.getClass());
		Mapx<String, Object> map = new Mapx<String, Object>();
		convertedMap.put(bean, map);
		for (Entry<String, BeanProperty> e : bim.getPropertyMap().entrySet()) {
			BeanProperty bp = e.getValue();
			if (bp.canRead() && (!pairFlag || bp.canWrite())) {
				if (bp.readMethod != null && bp.readMethod.getDeclaringClass() == Object.class) {
					continue;
				}
				String k = e.getKey();
				Object v = convert(bp.read(bean), pairFlag, convertedMap);
				map.put(k, v);
			}
		}
		return map;
	}

	private static Object convert(Object v, boolean pairFlag, HashMap<Object, Object> convertedMap) {
		if (v == null || Primitives.isPrimitives(v) || v instanceof String) {
			return v;
		}
		if (v instanceof Date || v instanceof Number) {
			return v;
		}
		if (convertedMap.containsKey(v)) {
			return convertedMap.get(v);
		}
		if (v.getClass().isArray()) {
			Object[] arr = new Object[Array.getLength(v)];
			convertedMap.put(v, arr);
			for (int i = 0; i < arr.length; i++) {
				arr[i] = convert(Array.get(v, i), pairFlag, convertedMap);
			}
			return arr;
		} else if (v instanceof Collection) {
			List<Object> list = new ArrayList<Object>();
			convertedMap.put(v, list);
			for (Object v2 : ((Collection<?>) v)) {
				list.add(convert(v2, pairFlag, convertedMap));
			}
			return list;
		} else if (v instanceof Map) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			convertedMap.put(v, map);
			for (Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
				map.put(e.getKey() == null ? null : e.getKey().toString(), convert(e.getValue(), pairFlag, convertedMap));
			}
		} else {
			v = toMap(v, pairFlag, convertedMap);
		}
		return v;
	}
}
