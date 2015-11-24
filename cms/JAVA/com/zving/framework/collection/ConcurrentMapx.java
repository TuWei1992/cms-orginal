package com.zving.framework.collection;

/**
 * 线程安全并且键值有顺序的Mapx
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-4-18
 */
public class ConcurrentMapx<K, V> extends Mapx<K, V> {
	private static final long serialVersionUID = 201404182133L;

	public ConcurrentMapx() {
		super(true);
	}

	public ConcurrentMapx(int initCapacity) {
		super(initCapacity, true);
	}
}
