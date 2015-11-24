package com.zving.framework.data.exception;

/**
 * 创建字段、索引、数据表异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class CreateException extends DDLException {

	private static final long serialVersionUID = 1L;

	public CreateException(Exception e) {
		super(e);
	}

}
