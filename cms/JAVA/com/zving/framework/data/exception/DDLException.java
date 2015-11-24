package com.zving.framework.data.exception;

/**
 * DLL执行异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public abstract class DDLException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public DDLException(Exception e) {
		super(e);
	}

}
