package com.zving.framework.security.exception;

import com.zving.framework.core.FrameworkException;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.action.AfterPrivCheckFailedAction;

/**
 * 权限异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-4
 */
public abstract class PrivException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public PrivException(String message) {
		super(message);
		ExtendManager.invoke(AfterPrivCheckFailedAction.ID, new Object[] { message });
	}

}
