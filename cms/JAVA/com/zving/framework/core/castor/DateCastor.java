package com.zving.framework.core.castor;

import java.util.Date;

import com.zving.framework.utility.DateUtil;

/**
 * 日期类型转换器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class DateCastor extends AbstractCastor {
	private static DateCastor singleton = new DateCastor();

	public static DateCastor getInstance() {
		return singleton;
	}

	private DateCastor() {
	}

	@Override
	public boolean canCast(Class<?> type) {
		return Date.class == type;
	}

	@Override
	public Object cast(Object obj, Class<?> type) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Date) {
			return obj;
		} else if (obj instanceof Long) {
			return new Date((Long) obj);
		} else {
			return DateUtil.parseDateTime(obj.toString());
		}
	}

}
