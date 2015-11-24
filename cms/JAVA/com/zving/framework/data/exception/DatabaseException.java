package com.zving.framework.data.exception;

import com.zving.framework.core.FrameworkException;

/**
 * 数据库异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class DatabaseException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public DatabaseException(Throwable e) {
		super(e);
	}

	public DatabaseException(String message) {
		super(message);
	}

}
