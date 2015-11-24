package com.zving.framework.extend.exception;

import com.zving.framework.core.FrameworkException;

/**
 * 创建扩展项实例异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-2-1
 */
public class CreateExtendItemInstanceException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public CreateExtendItemInstanceException(String message) {
		super(message);
	}

	public CreateExtendItemInstanceException(Throwable t) {
		super(t);
	}
}
