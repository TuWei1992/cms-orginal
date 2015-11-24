package com.zving.framework.core.castor;

/**
 * 整型数组转换器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class IntArrayCastor extends AbstractCastor {
	private static IntArrayCastor singleton = new IntArrayCastor();

	public static IntArrayCastor getInstance() {
		return singleton;
	}

	private IntArrayCastor() {
	}

	@Override
	public boolean canCast(Class<?> type) {
		return Integer[].class == type || int[].class == type;
	}

	@Override
	public Object cast(Object obj, Class<?> type) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof int[]) {
			return obj;
		}

		if (obj.getClass().isArray()) {
			Object[] os = (Object[]) obj;
			int[] arr = new int[os.length];
			for (int i = 0; i < os.length; i++) {
				arr[i] = (Integer) IntCastor.getInstance().cast(os[i], type);
			}
			return arr;
		} else if (obj instanceof String && DoubleArrayCastor.isNumberArray(obj.toString())) {
			double[] ds = DoubleArrayCastor.toDoubleArray(obj.toString());
			int[] arr = new int[ds.length];
			for (int i = 0; i < ds.length; i++) {
				arr[i] = new Double(ds[i]).intValue();
			}
			return arr;
		} else {
			return new int[] { (Integer) IntCastor.getInstance().cast(obj, type) };
		}
	}
}
