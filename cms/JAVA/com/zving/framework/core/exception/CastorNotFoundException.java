package com.zving.framework.core.exception;

import com.zving.framework.core.FrameworkException;

/**
 * 类型转换器未找到异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class CastorNotFoundException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public CastorNotFoundException(String message) {
		super(message);
	}
}
