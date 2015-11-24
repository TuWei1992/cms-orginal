package com.zving.framework.core;

/**
 * ZCF框架异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class FrameworkException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FrameworkException(String message) {
		super(message);
	}

	public FrameworkException(Throwable t) {
		super(t);
	}

	public FrameworkException(String message, Throwable cause) {
		super(message, cause);
	}

}
