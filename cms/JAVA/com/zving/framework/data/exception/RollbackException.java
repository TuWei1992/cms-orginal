package com.zving.framework.data.exception;

/**
 * 事务回滚异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class RollbackException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public RollbackException(Exception e) {
		super(e);
	}

}
