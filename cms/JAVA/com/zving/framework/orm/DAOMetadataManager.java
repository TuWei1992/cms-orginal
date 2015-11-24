package com.zving.framework.orm;

import com.zving.framework.collection.ConcurrentMapx;

/**
 * DAO元数据管理器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-8
 */
public class DAOMetadataManager {
	static ConcurrentMapx<String, DAOMetadata> map = new ConcurrentMapx<String, DAOMetadata>();

	@SuppressWarnings("rawtypes")
	public static DAOMetadata getMetadata(Class<? extends DAO> clazz) {
		String className = clazz.getName();
		if (!map.containsKey(className)) {
			DAOMetadata dm = new DAOMetadata(clazz);
			map.put(className, dm);
			return dm;
		}
		return map.get(className);
	}
}
