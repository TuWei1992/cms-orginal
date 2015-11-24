package com.zving.framework.core.method;

import java.lang.reflect.Method;

import com.zving.framework.Current;
import com.zving.framework.core.bean.BeanDescription;
import com.zving.framework.core.bean.BeanManager;
import com.zving.framework.core.bean.BeanProperty;
import com.zving.framework.core.bean.BeanUtil;
import com.zving.framework.thirdparty.el.Constants;
import com.zving.framework.utility.Primitives;

/**
 * UI方法参数绑定器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-6
 */
public class UIMethodBinder {
	/**
	 * 将参数绑定到UIMethod实例（将当前请求中的变量设置到UIMethod的属性上，并将args中的值按类型匹配到方法参数上）
	 * 
	 * @param m UIMethod实例
	 * @param args 参数列表
	 */
	public static void bind(UIMethod m, Object[] args) {
		BeanUtil.fill(m, Current.getRequest());
		BeanDescription bd = BeanManager.getBeanDescription(m.getClass());
		for (BeanProperty bp : bd.getPropertyMap().values()) {
			if (bp.canWrite()) {
				for (Object arg : args) {
					if (!Primitives.isPrimitives(arg) && !(arg instanceof String) && bp.getPropertyType().isInstance(arg)) {
						bp.write(m, arg);
					}
				}
			}
		}
	}

	/**
	 * @param m 方法
	 * @param args 参数列表
	 * @return 将args转换成匹配方法参数类型的数组
	 */
	public static Object[] convertArg(Method m, Object[] args) {
		Class<?>[] cs = m.getParameterTypes();
		if (cs == null || cs.length == 0) {
			return Constants.NoArgs;
		}
		Object[] arr = new Object[cs.length];
		int i = 0;
		for (Class<?> c : cs) {
			for (Object obj : args) {
				if (c.isInstance(obj)) {
					arr[i] = obj;
					break;
				}
			}
			i++;
		}
		return arr;
	}
}
