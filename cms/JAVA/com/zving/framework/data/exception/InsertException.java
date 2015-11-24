package com.zving.framework.data.exception;

/**
 * 插入数据异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class InsertException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public InsertException(Exception e) {
		super(e);
	}

	public InsertException(String message) {
		super(message);
	}

}
