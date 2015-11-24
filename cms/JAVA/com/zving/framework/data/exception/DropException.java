package com.zving.framework.data.exception;

/**
 * 删除字段、索引、键、数据表异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class DropException extends DDLException {

	private static final long serialVersionUID = 1L;

	public DropException(Exception e) {
		super(e);
	}

}
