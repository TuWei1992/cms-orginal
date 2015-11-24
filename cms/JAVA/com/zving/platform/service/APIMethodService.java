package com.zving.platform.service;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.platform.IAPIMethod;

/**
 * API方法扩展服务
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-9-11
 */
public class APIMethodService extends AbstractExtendService<IAPIMethod> {
	public static final String ID = "com.zving.platform.service.APIMethodService";

	public static APIMethodService getInstance() {
		APIMethodService dts = findInstance(APIMethodService.class);
		return dts;
	}
}
