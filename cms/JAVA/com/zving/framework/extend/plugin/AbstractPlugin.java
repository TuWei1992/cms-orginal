package com.zving.framework.extend.plugin;

/**
 * 插件虚拟类
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-7-15
 */
public abstract class AbstractPlugin implements IPlugin {
	private PluginConfig config;

	@Override
	public void install() throws PluginException {
	}

	@Override
	public void uninstall() throws PluginException {
	}

	@Override
	public PluginConfig getConfig() {
		return config;
	}

	public void setConfig(PluginConfig config) {
		this.config = config;
	}

	@Override
	public void destory() {

	}
}
