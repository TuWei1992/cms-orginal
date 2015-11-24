package com.zving.framework.data.exception;

/**
 * 设置Statement变量异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class SetParamException extends DatabaseException {

	private static final long serialVersionUID = 1L;

	public SetParamException(Exception e) {
		super(e);
	}

}
