package com.zving.framework.core.exception;

import com.zving.framework.core.FrameworkException;

/**
 * Bean初始化异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class BeanInitException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public BeanInitException(String message) {
		super(message);
	}
}
