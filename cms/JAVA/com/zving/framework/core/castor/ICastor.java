package com.zving.framework.core.castor;

import com.zving.framework.extend.IExtendItem;

/**
 * 类型转换器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public interface ICastor extends IExtendItem {
	/**
	 * @param type 类型
	 * @return 指定类型是否可以由本实例转换
	 */
	public boolean canCast(Class<?> type);

	/**
	 * @param obj 待转换的对象
	 * @param type 目标类型
	 * @return 将对象转换成目标类型
	 */
	public Object cast(Object obj, Class<?> type);
}
