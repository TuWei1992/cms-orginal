package com.zving.framework.data.exception;

/**
 * 修改字段、主键异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class AlterException extends DDLException {

	private static final long serialVersionUID = 1L;

	public AlterException(Exception e) {
		super(e);
	}

}
