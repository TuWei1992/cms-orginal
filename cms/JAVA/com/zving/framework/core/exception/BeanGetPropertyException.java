package com.zving.framework.core.exception;

import com.zving.framework.core.FrameworkException;

/**
 * Bean获取属性异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class BeanGetPropertyException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public BeanGetPropertyException(Exception e) {
		super(e);
	}
}
