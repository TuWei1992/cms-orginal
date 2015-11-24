package com.zving.platform.action;

import com.zving.framework.extend.action.BeforeCronTaskExecutedAction;
import com.zving.platform.bl.LogBL;
import com.zving.platform.log.type.CronLog;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class BeforeCronTaskExecuted extends BeforeCronTaskExecutedAction {

	@Override
	public void execute(String taskManagerID, String taskID) {
		//添加定时任务完成日志
		LogBL.addLog(CronLog.ID, CronLog.SubType_Start, taskID + " start!");
	}

	@Override
	public boolean isUsable() {
		return true;
	}
}