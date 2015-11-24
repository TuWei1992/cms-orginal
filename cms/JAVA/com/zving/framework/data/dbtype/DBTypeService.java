package com.zving.framework.data.dbtype;

import com.zving.framework.extend.AbstractExtendService;

/**
 * 数据库类型扩展服务
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-5-6
 */
public class DBTypeService extends AbstractExtendService<IDBType> {
	private static DBTypeService instance = null;

	public static DBTypeService getInstance() {
		if (instance == null) {
			instance = findInstance(DBTypeService.class);
		}
		return instance;
	}
}
