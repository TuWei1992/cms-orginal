package com.zving.framework.core.castor;

import com.zving.framework.utility.ObjectUtil;

/**
 * 双字节浮点类型转换器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class DoubleCastor extends AbstractCastor {
	private static DoubleCastor singleton = new DoubleCastor();

	public static DoubleCastor getInstance() {
		return singleton;
	}

	private DoubleCastor() {
	}

	@Override
	public boolean canCast(Class<?> type) {
		return Double.class == type || double.class == type;
	}

	@Override
	public Object cast(Object obj, Class<?> type) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		} else {
			String str = obj.toString();
			if (ObjectUtil.empty(str) || str.equals("null") || str.equals("undefined")) {
				return 0;
			}
			try {
				return Double.parseDouble(obj.toString());
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		return 0D;
	}

}
