package com.zving.framework.core.exception;

import com.zving.framework.core.FrameworkException;

/**
 * UIMethod异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-5
 */
public class UIMethodException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public UIMethodException(String message) {
		super(message);
	}

	public UIMethodException(Throwable e) {
		super(e);
	}
}
