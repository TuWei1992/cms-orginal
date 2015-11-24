package com.zving.platform;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.platform.service.EventTypeService;
import com.zving.platform.util.PlatformUtil;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-9
 */
public class PlatformPlugin extends FrameworkPlugin {
	public static final String ID = "com.zving.platform";

	@Override
	public void start() {
		// 加载ZDConfig中的配置项
		PlatformUtil.loadDBConfig();
		// Event事件通知
		EventTypeService.start();
	}
}
