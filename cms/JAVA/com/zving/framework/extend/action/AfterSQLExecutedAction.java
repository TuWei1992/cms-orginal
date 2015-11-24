package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * SQL执行后执行
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public abstract class AfterSQLExecutedAction implements IExtendAction {
	public static final String ID = "com.zving.framework.AfterSQLExecutedAction";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		long costTime = (Long) args[0];
		String message = (String) args[1];
		execute(costTime, message);
		return null;
	}

	public abstract void execute(long costTime, String message);
}
