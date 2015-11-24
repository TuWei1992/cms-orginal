package com.zving.framework.cache;

import java.util.Map;

import com.zving.framework.collection.CacheMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.collection.ReadOnlyMapx;
import com.zving.framework.utility.LogUtil;

/**
 * @Author 王育春
 * @Date 2008-10-30
 * @Mail wyuch@zving.com
 */
public class CacheManager {
	private static CacheMapx<String, CacheMapx<String, String>> loadedTypes = new CacheMapx<String, CacheMapx<String, String>>(); // 已经加载过的type集合

	/**
	 * 获取指定类型的的CacheProvider
	 */
	public static CacheDataProvider getCache(String providerID) {
		return CacheService.getInstance().get(providerID);
	}

	public static Object get(String providerID, String type, long key) {
		return get(providerID, type, String.valueOf(key));
	}

	private static void onTypeNotFound(CacheDataProvider cp, String type) {
		cp.OnNotFound = true;
		try {
			cp.onTypeNotFound(type);
		} finally {
			cp.OnNotFound = false;
		}
	}

	private static void onKeyNotFound(CacheDataProvider cp, String type, String key) {
		cp.OnNotFound = true;
		try {
			cp.onKeyNotFound(type, key);
		} finally {
			cp.OnNotFound = false;
		}
	}

	public static Object get(String providerID, String type, String key) {
		CacheDataProvider cp = getCache(providerID);
		if (cp == null || cp.TypeMap == null) {
			throw new RuntimeException("CacheProvider not found:" + providerID);
		}
		Map<String, Object> map = cp.TypeMap.get(type);
		if (map == null && !isTypeLoaded(cp.getExtendItemID(), type)) {
			cp.Lock.lock();
			try {
				map = cp.TypeMap.get(type);
				if (map == null && !isTypeLoaded(cp.getExtendItemID(), type)) {
					onTypeNotFound(cp, type);
					map = cp.TypeMap.get(type);
					setLoadedType(cp.getExtendItemID(), type);
					if (map == null) {
						LogUtil.warn("CacheManager.get():Can't found cache type '" + type + "' in CacheProvider " + providerID);
						return null;
					}
				}
			} finally {
				cp.Lock.unlock();
			}
		}
		if (map == null) {
			return null;
		}
		if (!map.containsKey(key)) {
			cp.Lock.lock();
			try {
				if (!map.containsKey(key)) {
					onKeyNotFound(cp, type, key);
				}
				if (!map.containsKey(key)) {
					LogUtil.warn("Get cache data failed: Provider=" + providerID + ",Type=" + type + ",Key=" + key);
					// LogUtil.warn(ObjectUtil.getCurrentStack());
					return null;
				}
			} finally {
				cp.Lock.unlock();
			}
		}
		return map.get(key);
	}

	/**
	 * 是否存在指定键值
	 */
	public static boolean contains(String providerID, String type, Object key) {
		CacheDataProvider cp = getCache(providerID);
		if (cp == null) {
			return false;
		}
		Map<String, Object> map = cp.TypeMap.get(type);
		if (map == null && !isTypeLoaded(cp.getExtendItemID(), type)) {
			cp.Lock.lock();
			try {
				map = cp.TypeMap.get(type);
				if (map == null && !isTypeLoaded(cp.getExtendItemID(), type)) {
					onTypeNotFound(cp, type);
					map = cp.TypeMap.get(type);
					setLoadedType(cp.getExtendItemID(), type);
				}
			} finally {
				cp.Lock.unlock();
			}
		}
		if (map == null) {
			return false;
		}
		String strKey = String.valueOf(key);
		if (!map.containsKey(strKey)) {
			cp.Lock.lock();
			try {
				if (!map.containsKey(strKey)) {
					onKeyNotFound(cp, type, strKey);
				}
				if (!map.containsKey(strKey)) {
					return false;
				}
			} finally {
				cp.Lock.unlock();
			}
		}
		return true;
	}

	public static void set(String providerID, String type, long key, Object value) {
		set(providerID, type, String.valueOf(key), value);
	}

	public static void set(String providerID, String type, String key, Object value) {
		CacheDataProvider cp = getCache(providerID);
		if (cp == null) {
			LogUtil.warn("未找到CacheProvider:" + providerID);
			return;
		}
		Map<String, Object> map = cp.TypeMap.get(type);
		if (map == null && !isTypeLoaded(cp.getExtendItemID(), type)) {
			cp.Lock.lock();
			try {
				map = cp.TypeMap.get(type);
				if (map == null && !isTypeLoaded(cp.getExtendItemID(), type)) {
					onTypeNotFound(cp, type);
					map = cp.TypeMap.get(type);
					setLoadedType(cp.getExtendItemID(), type);
					if (map == null) {
						LogUtil.warn("CacheManager.get():Can't found cache type '" + type + "' in CacheProvider " + providerID);
						return;
					}
				}
			} finally {
				cp.Lock.unlock();
			}
		}
		if (map == null) {
			return;
		}
		cp.Lock.lock();
		try {
			map.put(key, value);
			if (CacheSyncUtil.enabled() && !cp.OnNotFound) {// Memcached集群下需要删除集群缓存
				CacheSyncUtil.refresh(cp.getExtendItemID(), type);
			}
			cp.onKeySet(type, key, value);
		} finally {
			cp.Lock.unlock();
		}
	}

	/**
	 * 删除缓存数据
	 */
	public static void remove(String providerID, String type, long key) {
		remove(providerID, type, String.valueOf(key));
	}

	public static void remove(String providerID, String type, String key) {
		CacheDataProvider cp = getCache(providerID);
		if (cp == null) {
			LogUtil.warn("CacheProvider not found:" + providerID);
			return;
		}
		Map<String, Object> map = cp.TypeMap.get(type);
		if (map == null) {
			LogUtil.warn("CacheManager.remove():Can't found cache type '" + type + "' in CacheProvider " + providerID);
			return;
		}
		cp.Lock.lock();
		try {
			map.remove(key);
			if (CacheSyncUtil.enabled()) {// Memcached集群下需要删除集群缓存
				CacheSyncUtil.refresh(cp.getExtendItemID(), type, key);
			}
		} finally {
			cp.Lock.unlock();
		}
	}

	/**
	 * 删除缓存类型，也可以通过本方法来更新整个类型的缓存
	 */
	public static void removeType(String providerID, String type) {
		CacheDataProvider cp = getCache(providerID);
		if (cp == null) {
			LogUtil.warn("CacheProvider not found:" + providerID);
			return;
		}
		cp.Lock.lock();
		try {
			cp.TypeMap.remove(type);
			CacheMapx<String, String> map = loadedTypes.get(cp.getExtendItemID());
			if (map != null) {
				map.remove(type);
			}
			if (CacheSyncUtil.enabled()) {// Memcached集群下需要删除集群缓存
				CacheSyncUtil.refresh(cp.getExtendItemID(), type);
			}
		} finally {
			cp.Lock.unlock();
		}
	}

	/**
	 * 获取缓存类型对应的Mapx(被包装成ReadOnlyMapx的实例)。<br>
	 * 注意：可能缓存中只有同一类型的一部分数据，其它数据要等待延迟加载。
	 */
	public static Mapx<String, Object> getMapx(String providerID, String type) {
		CacheDataProvider cp = getCache(providerID);
		if (cp == null) {
			LogUtil.warn("CacheProvider not found:" + providerID);
			return null;
		}
		Map<String, Object> map = cp.TypeMap.get(type);
		if (map == null && !isTypeLoaded(cp.getExtendItemID(), type)) {
			cp.Lock.lock();
			try {
				map = cp.TypeMap.get(type);
				if (map == null && !isTypeLoaded(providerID, type)) {
					onTypeNotFound(cp, type);
					map = cp.TypeMap.get(type);
					setLoadedType(cp.getExtendItemID(), type);
					if (map == null) {
						LogUtil.warn("CacheManager.getMapx():Can't found cache type '" + type + "' in CacheProvider " + providerID);
						return null;
					}
				}
			} finally {
				cp.Lock.unlock();
			}
		}
		if (map == null) {
			return null;
		}
		return new ReadOnlyMapx<String, Object>(map);
	}

	/**
	 * 设置缓存类型对应的Mapx
	 */
	public static void setMapx(String providerID, String type, CacheMapx<String, Object> map) {
		CacheDataProvider cp = getCache(providerID);
		if (cp == null) {
			LogUtil.warn("CacheProvider not found:" + providerID);
			return;
		}
		setMapx(cp, type, map);
		if (CacheSyncUtil.enabled() && !cp.OnNotFound) {// Memcached集群下需要删除集群缓存
			CacheSyncUtil.refresh(cp.getExtendItemID(), type);
		}
	}

	static void setMapx(CacheDataProvider cp, String type, CacheMapx<String, Object> map) {
		cp.Lock.lock();
		try {
			if (map instanceof CacheMapx) {
				cp.TypeMap.put(type, map);
			} else {
				CacheMapx<String, Object> r = new CacheMapx<String, Object>();
				if (map != null) {
					r.putAll(map);
				}
				cp.TypeMap.put(type, r);
			}
		} finally {
			cp.Lock.unlock();
		}
	}

	/**
	 * 获取缓存类型对应的Mapx，请使用getMapx(String providerID, String type)代替
	 * 
	 * @deprecated
	 */
	@Deprecated
	public static Map<String, Object> get(String providerID, String type) {
		return getMapx(providerID, type);
	}

	public static void destory() {
		for (CacheDataProvider cdp : CacheService.getInstance().getAll()) {
			cdp.destory();
		}
	}

	private static boolean isTypeLoaded(String providerid, String type) {
		CacheMapx<String, String> map = loadedTypes.get(providerid);
		if (map == null) {
			map = new CacheMapx<String, String>();
			loadedTypes.put(providerid, map);
		}
		return map.containsKey(type);
	}

	private static void setLoadedType(String providerid, String type) {
		CacheMapx<String, String> map = loadedTypes.get(providerid);
		if (map == null) {
			map = new CacheMapx<String, String>();
		}
		if (map.containsKey(type)) {
			LogUtil.warn("There is a duplicate type name in provider :" + providerid);
		}
		map.put(type, "");
		loadedTypes.put(providerid, map);
	}
}
