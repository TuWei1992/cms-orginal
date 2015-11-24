package com.zving.framework.core.exception;

import com.zving.framework.core.FrameworkException;

/**
 * Bean设置属性值异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class BeanSetPropertyException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public BeanSetPropertyException(Exception e) {
		super(e);
	}
}
