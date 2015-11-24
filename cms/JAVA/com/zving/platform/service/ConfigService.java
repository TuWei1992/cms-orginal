package com.zving.platform.service;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.platform.FixedConfigItem;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-17
 */
public class ConfigService extends AbstractExtendService<FixedConfigItem> {
	public static ConfigService getInstance() {
		return findInstance(ConfigService.class);
	}
}
