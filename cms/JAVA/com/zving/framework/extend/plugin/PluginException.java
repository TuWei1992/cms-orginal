package com.zving.framework.extend.plugin;

/**
 * 插件异常
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-7-27
 */
public class PluginException extends Exception {
	private static final long serialVersionUID = 1L;

	private String message;

	public PluginException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
