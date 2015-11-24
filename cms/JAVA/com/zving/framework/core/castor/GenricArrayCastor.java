package com.zving.framework.core.castor;

import java.lang.reflect.Array;

/**
 * 数组类型转换器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class GenricArrayCastor extends AbstractCastor {
	private static GenricArrayCastor singleton = new GenricArrayCastor();

	public static GenricArrayCastor getInstance() {
		return singleton;
	}

	private GenricArrayCastor() {
	}

	@Override
	public boolean canCast(Class<?> type) {
		return type.isArray();
	}

	@Override
	public Object cast(Object obj, Class<?> type) {// type表示一个数组class，例如ICastor[].class
		if (obj == null) {
			return null;
		}
		if (obj.getClass().isArray()) {
			if (type.isAssignableFrom(obj.getClass())) {
				return obj;
			}
			Object[] os = (Object[]) obj;
			Object[] arr = (Object[]) Array.newInstance(type.getComponentType(), os.length);
			for (int i = 0; i < os.length; i++) {
				arr[i] = os[i] == null ? null : CastorService.toType(obj, type.getComponentType());
			}
			return arr;
		} else {
			return new Object[] { CastorService.toType(obj, type.getComponentType()) };
		}
	}
}
