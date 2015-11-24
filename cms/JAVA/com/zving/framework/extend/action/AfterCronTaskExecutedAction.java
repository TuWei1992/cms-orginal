package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * 在定时任务执行后扫行
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public abstract class AfterCronTaskExecutedAction implements IExtendAction {
	public static final String ID = "com.zving.framework.AfterCronTaskExecutedAction";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		String taskManagerID = (String) args[0];
		String taskID = (String) args[1];
		execute(taskManagerID, taskID);
		return null;
	}

	public abstract void execute(String taskManagerID, String taskID);
}
