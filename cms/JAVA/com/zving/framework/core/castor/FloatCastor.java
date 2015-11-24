package com.zving.framework.core.castor;

import com.zving.framework.utility.ObjectUtil;

/**
 * 浮点类型转换器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class FloatCastor extends AbstractCastor {
	private static FloatCastor singleton = new FloatCastor();

	public static FloatCastor getInstance() {
		return singleton;
	}

	private FloatCastor() {
	}

	@Override
	public boolean canCast(Class<?> type) {
		return Float.class == type || float.class == type;
	}

	@Override
	public Object cast(Object obj, Class<?> type) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).floatValue();
		} else {
			String str = obj.toString();
			if (ObjectUtil.empty(str) || str.equals("null") || str.equals("undefined")) {
				return 0F;
			}
			try {
				return Float.parseFloat(obj.toString());
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		return 0F;
	}

}
