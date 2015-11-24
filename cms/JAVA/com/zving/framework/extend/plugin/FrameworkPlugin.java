package com.zving.framework.extend.plugin;

import com.zving.framework.i18n.LangMapping;

/**
 * 代表框架本身的插件。<br>
 * 本插件属于特殊插件，不能被安装、启动、停止和卸载。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-7-27
 */
public class FrameworkPlugin extends AbstractPlugin {
	public static final String ID = "com.zving.framework";

	@Override
	public void install() throws PluginException {
		throw new PluginException(LangMapping.get("Framework.Plugin.InstallFail"));
	}

	@Override
	public void start() throws PluginException {
	}

	@Override
	public void stop() throws PluginException {
		throw new PluginException(LangMapping.get("Framework.Plugin.StopFail"));
	}

	@Override
	public void uninstall() throws PluginException {
		throw new PluginException(LangMapping.get("Framework.Plugin.UninstallFail"));
	}

}
