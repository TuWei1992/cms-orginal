package com.zving.framework.template.exception;

import com.zving.framework.core.FrameworkException;

/**
 * 模板异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public class TemplateException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public TemplateException(String message) {
		super(message);
	}

	public TemplateException(Throwable t) {
		super(t);
	}

	public TemplateException(String message, Throwable cause) {
		super(message, cause);
	}

}
