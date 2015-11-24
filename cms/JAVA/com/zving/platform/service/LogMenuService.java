package com.zving.platform.service;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.platform.ILogMenu;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
public class LogMenuService extends AbstractExtendService<ILogMenu> {
	public static LogMenuService getInstance() {
		return findInstance(LogMenuService.class);
	}
}
