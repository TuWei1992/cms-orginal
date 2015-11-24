package com.zving.framework.core.castor;

/**
 * 布尔数组类型转换器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class BooleanArrayCastor extends AbstractCastor {
	private static BooleanArrayCastor singleton = new BooleanArrayCastor();

	private BooleanArrayCastor() {
	}

	public static BooleanArrayCastor getInstance() {
		return singleton;
	}

	@Override
	public boolean canCast(Class<?> type) {
		return Boolean[].class == type || boolean[].class == type;
	}

	@Override
	public Object cast(Object obj, Class<?> type) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof boolean[]) {
			return obj;
		} else if (obj.getClass().isArray()) {
			Object[] os = (Object[]) obj;
			boolean[] arr = new boolean[os.length];
			for (int i = 0; i < os.length; i++) {
				arr[i] = (Boolean) BooleanCastor.getInstance().cast(os[i], type);
			}
			return arr;
		} else {
			return new boolean[] { (Boolean) BooleanCastor.getInstance().cast(obj, type) };
		}
	}

}
