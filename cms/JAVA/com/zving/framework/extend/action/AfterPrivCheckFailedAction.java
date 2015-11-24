package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * 权限检查失败后执行
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public abstract class AfterPrivCheckFailedAction implements IExtendAction {
	public static final String ID = "com.zving.framework.AfterPrivCheckFailedAction";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		String message = (String) args[0];
		execute(message);
		return null;
	}

	public abstract void execute(String message);

}
