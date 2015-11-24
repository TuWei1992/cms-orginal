package com.zving.framework.core.castor;

import java.util.Date;

import com.zving.framework.core.exception.CastorNotFoundException;
import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.extend.IExtendItem;
import com.zving.framework.utility.ObjectUtil;

/**
 * 类型转换器扩展服务
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class CastorService extends AbstractExtendService<ICastor> {
	public static CastorService getInstance() {
		return AbstractExtendService.findInstance(CastorService.class);
	}

	public static Object toType(Object obj, Class<?> type) {
		if (obj == null) {
			if (ObjectUtil.in(type, Integer.class, int.class, Long.class, long.class, Float.class, float.class, Double.class, double.class)) {
				return 0;
			}
			return obj;
		}
		if (type.isInstance(obj)) {
			return obj;
		}
		if (type == String.class) {
			return obj.toString();
		}
		if (type == Integer.class || type == int.class) {
			return IntCastor.getInstance().cast(obj, type);
		} else if (type == Long.class || type == long.class) {
			return LongCastor.getInstance().cast(obj, type);
		} else if (type == Float.class || type == float.class) {
			return FloatCastor.getInstance().cast(obj, type);
		} else if (type == Double.class || type == double.class) {
			return DoubleCastor.getInstance().cast(obj, type);
		} else if (type == Boolean.class || type == boolean.class) {
			return BooleanCastor.getInstance().cast(obj, type);
		} else if (type == Date.class) {
			return DateCastor.getInstance().cast(obj, type);
		} else if (type == String[].class) {
			return StringArrayCastor.getInstance().cast(obj, type);
		} else if (type == int[].class || type == Integer[].class) {
			return StringArrayCastor.getInstance().cast(obj, type);
		} else if (type == long[].class || type == Long[].class) {
			return LongArrayCastor.getInstance().cast(obj, type);
		} else if (type == float[].class || type == Float[].class) {
			return FloatArrayCastor.getInstance().cast(obj, type);
		} else if (type == double[].class || type == Double[].class) {
			return DoubleArrayCastor.getInstance().cast(obj, type);
		} else if (type == boolean[].class || type == Boolean[].class) {
			return BooleanArrayCastor.getInstance().cast(obj, type);
		} else {
			for (ICastor castor : getInstance().getAll()) {
				if (castor.canCast(type)) {
					return castor.cast(obj, type);
				}
			}
		}
		throw new CastorNotFoundException(type.getClass().getName());
	}

	private boolean innerCastorLoaded = false;

	/**
	 * 加载内置的Castor
	 */
	private void init() {
		if (!innerCastorLoaded) {
			register(IntCastor.getInstance());
			register(LongCastor.getInstance());
			register(FloatCastor.getInstance());
			register(DoubleCastor.getInstance());
			register(DateCastor.getInstance());
			register(IntArrayCastor.getInstance());
			register(LongArrayCastor.getInstance());
			register(FloatArrayCastor.getInstance());
			register(DoubleArrayCastor.getInstance());
			register(BooleanCastor.getInstance());
			register(BooleanArrayCastor.getInstance());
			register(StringArrayCastor.getInstance());
			register(GenricArrayCastor.getInstance());
			innerCastorLoaded = true;
		}
	}

	@Override
	public void register(IExtendItem item) {
		init();
		super.register(item);
	}
}
