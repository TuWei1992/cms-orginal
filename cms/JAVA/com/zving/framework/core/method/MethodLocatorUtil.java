package com.zving.framework.core.method;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Priv;
import com.zving.framework.annotation.util.AliasMapping;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.exception.UIMethodNotFoundException;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 定位器工具类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-6
 */
public class MethodLocatorUtil {
	static ConcurrentMapx<String, IMethodLocator> map = new ConcurrentMapx<String, IMethodLocator>();

	/**
	 * @param fullName 方法全路径
	 * @return 方法全路径对应的定位器
	 */
	@SuppressWarnings("unchecked")
	private static IMethodLocator create(String fullName) {
		try {
			if (fullName.indexOf("$") > 0) {
				Class<?> clazz = Class.forName(fullName);
				if (!Modifier.isPublic(clazz.getModifiers())) {
					return null;
				} else if (Modifier.isStatic(clazz.getModifiers())) {
					return null;
				} else if (!UIMethod.class.isAssignableFrom(clazz)) {
					return null;
				} else if (!UIFacade.class.isAssignableFrom(clazz.getDeclaringClass())) {
					return null;
				}
				return new FacadeInnerClassLocator((Class<? extends UIMethod>) clazz);
			} else {
				if (fullName.startsWith("com.zving.framework.")) {// 框架下的文件默认都使用了全路径
					int j = fullName.lastIndexOf(".");
					fullName = fullName.substring(0, j) + "#" + fullName.substring(j + 1);
				}
				int i = fullName.lastIndexOf("#");
				if (i < 0) {// 类
					Class<?> clazz = Class.forName(fullName);
					if (!UIMethod.class.isAssignableFrom(clazz)) {
						return null;
					}
					return new MethodClassLocator((Class<? extends UIMethod>) clazz);
				} else {
					String className = fullName.substring(0, i);
					String methodName = fullName.substring(i + 1);
					Class<?> clazz = Class.forName(className);
					if (!UIFacade.class.isAssignableFrom(clazz)) {
						return null;
					}
					for (Method m : clazz.getMethods()) {
						if (!Modifier.isPublic(m.getModifiers())) {
							continue;
						} else if (Modifier.isStatic(m.getModifiers())) {
							continue;
						} else if (!m.isAnnotationPresent(Priv.class)) {
							continue;
						}
						if (m.getName().equals(methodName)) {
							return new FacadeMemberMethodLocator(m);
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			LogUtil.error("UIMethod class [" + fullName + "] not found!");
		}
		return null;
	}

	/**
	 * @param alias 方法别名
	 * @return 别名对应的定位器
	 */
	public static IMethodLocator find(String alias) {
		int i = alias.indexOf("?");
		if (i > 0) {
			String params = alias.substring(i + 1);
			alias = alias.substring(0, i);
			Mapx<String, String> map = StringUtil.splitToMapx(params, "&", "=");
			if (Current.getRequest() != null) {
				Current.getRequest().putAll(map);
			}
		}
		String fullName = AliasMapping.get(alias);
		IMethodLocator m = map.get(fullName);
		if (m == null) {
			m = create(fullName);
			map.put(fullName, m);
			if (m == null) {
				throw new UIMethodNotFoundException(alias);
			}
		}
		Current.setMethod(m);
		return m;
	}
}
