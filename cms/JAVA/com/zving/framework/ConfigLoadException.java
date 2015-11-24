package com.zving.framework;

import com.zving.framework.core.FrameworkException;

/**
 * 配置文件加载异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-10
 */
public class ConfigLoadException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public ConfigLoadException(String message) {
		super(message);
	}

}
