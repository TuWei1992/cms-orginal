package com.zving.framework.security.exception;

/**
 * 会员未登录异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class MemberNotLoginException extends PrivException {
	private static final long serialVersionUID = 1L;

	public MemberNotLoginException(String message) {
		super(message);
	}
}
