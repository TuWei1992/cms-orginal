package com.zving.platform.service;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.platform.ILogType;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
public class LogTypeService extends AbstractExtendService<ILogType> {
	public static LogTypeService getInstance() {
		return findInstance(LogTypeService.class);
	}
}
