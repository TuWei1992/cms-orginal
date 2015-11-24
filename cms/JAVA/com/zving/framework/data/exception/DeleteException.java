package com.zving.framework.data.exception;

/**
 * 删除数据异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class DeleteException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public DeleteException(Exception e) {
		super(e);
	}

	public DeleteException(String message) {
		super(message);
	}

}
