package com.zving.framework.data.exception;

/**
 * 数据库连接关闭异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class CloseException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public CloseException(Exception e) {
		super(e);
	}

}
