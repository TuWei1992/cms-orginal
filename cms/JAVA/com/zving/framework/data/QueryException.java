package com.zving.framework.data;

import com.zving.framework.data.exception.DatabaseException;

/**
 * 查询数据异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-3
 */
public class QueryException extends DatabaseException {
	private static final long serialVersionUID = 1L;

	public QueryException(String message) {
		super(message);
	}

	public QueryException(Throwable e) {
		super(e);
	}

}
