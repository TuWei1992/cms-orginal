package com.zving.framework.security.exception;

/**
 * 用户未登录异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class UserNotLoginException extends PrivException {
	private static final long serialVersionUID = 1L;

	public UserNotLoginException(String message) {
		super(message);
	}
}