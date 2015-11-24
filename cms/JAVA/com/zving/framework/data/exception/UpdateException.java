package com.zving.framework.data.exception;

/**
 * 更新数据异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class UpdateException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public UpdateException(String message) {
		super(message);
	}

}
