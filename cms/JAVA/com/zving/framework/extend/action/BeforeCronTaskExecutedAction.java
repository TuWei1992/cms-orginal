package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * 在定时任务执行前执行
 * 
 * @author 张金灿
 * @mail zjc@zving.com
 * @date 2014-12-16
 */
public abstract class BeforeCronTaskExecutedAction implements IExtendAction {
	public static final String ID = "com.zving.framework.BeforeCronTaskExecutedAction";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		String taskManagerID = (String) args[0];
		String taskID = (String) args[1];
		execute(taskManagerID, taskID);
		return null;
	}

	public abstract void execute(String taskManagerID, String taskID);
}
