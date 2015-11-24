package com.zving.framework.cache;

/**
 * 缓存同步器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-10-28
 */
public interface ICacheSynchronizer {

	/**
	 * 刷新指定缓存数据提供者下指定类型的所有缓存项
	 */
	public void refresh(String providerID, String type);

	/**
	 * 刷新指定缓存数据提供者下指定类型下的指定缓存项
	 */
	public void refresh(String providerID, String type, String key);

	/**
	 * 发送同步数据，供集群中的其他服务器获取
	 */
	public void sync();
}
