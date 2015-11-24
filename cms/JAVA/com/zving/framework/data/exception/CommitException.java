package com.zving.framework.data.exception;

/**
 * 事务提交异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class CommitException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public CommitException(Exception e) {
		super(e);
	}

}
