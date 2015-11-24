package com.zving.framework.extend;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.ReadOnlyList;
import com.zving.framework.core.FrameworkException;

/**
 * 扩展服务虚拟类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-9
 */
public class AbstractExtendService<T extends IExtendItem> implements IExtendService<T> {
	protected CacheMapx<String, T> itemMap = new CacheMapx<String, T>();

	protected List<T> itemList = new ReadOnlyList<T>(new ArrayList<T>());

	/**
	 * 查找扩展服务的实例
	 */
	protected static <S extends IExtendService<?>> S findInstance(Class<S> clazz) {
		if (clazz == null) {
			throw new FrameworkException("ExtendService class can't be empty!");
		}
		ExtendServiceConfig config = ExtendManager.getInstance().findExtendServiceByClass(clazz.getName());
		if (config == null) {
			throw new FrameworkException("ExtendService not found,class is " + clazz.getName());
		}
		@SuppressWarnings("unchecked")
		S service = (S) config.getInstance();
		return service;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void register(IExtendItem item) {
		itemMap.put(item.getExtendItemID(), (T) item);
		prepareItemList();
	}

	@Override
	public T get(String id) {
		if (id == null) {
			return null;
		}
		return itemMap.get(id);
	}

	@Override
	public T remove(String id) {
		T ret = itemMap.remove(id);
		prepareItemList();
		return ret;
	}

	protected void prepareItemList() {
		itemList = new ReadOnlyList<T>(itemMap.values());
	}

	/**
	 * 注意：有可能返回null
	 */
	@Override
	public List<T> getAll() {
		return itemList;
	}

	public int size() {
		return itemList.size();
	}

	@Override
	public void destory() {
		itemMap.clear();
		itemMap = null;
		itemList = null;
	}

}
