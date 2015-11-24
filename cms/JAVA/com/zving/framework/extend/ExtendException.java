package com.zving.framework.extend;

/**
 * 扩展异常类
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-10
 */
public class ExtendException extends Exception {
	private static final long serialVersionUID = 1L;

	private String message;

	public ExtendException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
