package com.zving.framework.collection;

/**
 * 可传递参数的过滤器
 * 
 * @author 王育春
 * @date 2009-11-15
 * @email wangyc@zving.com
 */
public abstract class Filter<T> {
	protected Object param; // NO_UCD

	/**
	 * 创建一个空的过滤器对象
	 */
	public Filter() {
	}

	/**
	 * 创建一个有参数的过滤器对象
	 * 
	 * @param param
	 */
	public Filter(Object param) {// NO_UCD
		this.param = param;
	}

	/**
	 * 返回true表示保留，返回false表示过滤掉
	 */
	public abstract boolean filter(T obj);

}
