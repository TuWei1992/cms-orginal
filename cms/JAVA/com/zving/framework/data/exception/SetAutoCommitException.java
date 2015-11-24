package com.zving.framework.data.exception;

/**
 * 设置事务提交模式异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class SetAutoCommitException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public SetAutoCommitException(Exception e) {
		super(e);
	}

}
