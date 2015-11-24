package com.zving.platform.service;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.platform.ILogMenuGroup;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
public class LogMenuGroupService extends AbstractExtendService<ILogMenuGroup> {
	public static LogMenuGroupService getInstance() {
		return findInstance(LogMenuGroupService.class);
	}
}
