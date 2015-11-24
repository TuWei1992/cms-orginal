package com.zving.framework.security.exception;

/**
 * 未拥有权限项异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-20
 */
public class NoPrivException extends PrivException {
	private static final long serialVersionUID = 1L;

	public NoPrivException(String message) {
		super(message);
	}
}
