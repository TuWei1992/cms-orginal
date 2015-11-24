package com.zving.framework.orm;

import com.zving.framework.core.FrameworkException;

/**
 * DAO操作异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-8
 */
public class DAOException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public DAOException(String message) {
		super(message);
	}

}
