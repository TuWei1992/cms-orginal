package com.zving.framework.collection;

/**
 * 键值未找到事件监听器。<br>
 * 本监听器通过Mapx.setKeyNotFoundEventListener()方法设置到Mapx后，<br>
 * Mapx会在get()方法未获取到null并且键不存在时调用onKeyNotFound()，并将返回值加入到Mapx中
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-7-30
 */
public interface KeyNotFoundEventListener<K, V> {
	public V onKeyNotFound(K key);
}
