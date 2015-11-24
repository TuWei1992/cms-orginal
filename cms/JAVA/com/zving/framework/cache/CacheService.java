package com.zving.framework.cache;

import com.zving.framework.extend.AbstractExtendService;

/**
 * 缓存数据提供者扩展服务
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-9
 */
public class CacheService extends AbstractExtendService<CacheDataProvider> {
	public static CacheService getInstance() {
		return findInstance(CacheService.class);
	}
}
