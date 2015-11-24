package com.zving.framework.collection;

/**
 * 清除事件监听器，可以为Mapx等容器设置清除事件监听器，<br>
 * 当容器中有元素被清除时会调用监听器的onExit方法。
 * 
 * @Author 王育春
 * @Date 2009-4-28
 * @Mail wyuch@zving.com
 */
public abstract class ExitEventListener<K, V> {
	/**
	 * 键值对从容器中清除时会调用此方法
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void onExit(K key, V value);

}
