package com.zving.framework.core;

import com.zving.framework.extend.AbstractExtendService;

/**
 * Runtime异常捕获器扩展服务
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-20
 */
public class ExceptionCatcherService extends AbstractExtendService<IExceptionCatcher> {
	public static ExceptionCatcherService getInstance() {
		return AbstractExtendService.findInstance(ExceptionCatcherService.class);
	}
}
