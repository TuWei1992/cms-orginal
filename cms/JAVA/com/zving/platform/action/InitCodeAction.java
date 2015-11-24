package com.zving.platform.action;

import com.zving.framework.Config;
import com.zving.framework.extend.action.AfterAllPluginStartedAction;
import com.zving.platform.service.CodeService;

/**
 * 插件初始化完成后将注册代码持久化到数据库中
 * 
 * @author 李伟仪
 * @email lwy@zving.com
 * @date 2011-12-23
 */
public class InitCodeAction extends AfterAllPluginStartedAction {

	@Override
	public void execute() {
		// 用GBK编码跑了工程会导致ZDCode表数据乱码，暂时注掉
		if (Config.isInstalled()) {
			CodeService.init();
		}
	}

	@Override
	public boolean isUsable() {
		return true;
	}
}