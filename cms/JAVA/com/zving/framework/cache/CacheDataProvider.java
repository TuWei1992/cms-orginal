package com.zving.framework.cache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.collection.CacheMapx;
import com.zving.framework.extend.IExtendItem;

/**
 * 缓存数据提供者虚拟类
 * 
 * @Author 王育春
 * @Date 2008-10-30
 * @Mail wyuch@zving.com
 */
public abstract class CacheDataProvider implements IExtendItem {
	protected Lock Lock = new ReentrantLock();
	protected CacheMapx<String, CacheMapx<String, Object>> TypeMap = new CacheMapx<String, CacheMapx<String, Object>>();
	protected boolean OnNotFound = false;// 表明当前处于onKeyNotFound,OnTypeNotFound调用期间

	/**
	 * 当缓冲数据项置入时调用此方法。<br>
	 * 同一份数据在多个子类型中有键值时可以通过覆盖本方法避免多次载入，以提高性能。
	 * 
	 * @param type 子类型
	 * @param key 数据项键
	 * @param value 数据项值
	 */
	public void onKeySet(String type, String key, Object value) {
	}

	/**
	 * 当某个缓存子类型没有找到时调用此方法。<br>
	 * 子类通过实现此方法将一个子类型下的所有数据项一次性载入缓存。
	 * 
	 * @param type 子类型
	 */
	public abstract void onTypeNotFound(String type);

	/**
	 * 当某个数据项没有找到时调用此方法。<br>
	 * 子类通过实现本方法达到缓存加载数据项的效果。
	 * 
	 * @param type 子类型
	 * @param key 数据项键
	 */
	public abstract void onKeyNotFound(String type, String key);

	/**
	 * 销毁缓存数据
	 */
	public void destory() {
		for (CacheMapx<String, Object> map : TypeMap.values()) {
			map.clear();
			map = null;
		}
		TypeMap.clear();
		TypeMap = null;
	}
}
