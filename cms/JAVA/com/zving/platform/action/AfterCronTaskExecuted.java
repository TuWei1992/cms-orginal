package com.zving.platform.action;

import java.util.Date;

import com.zving.framework.data.Q;
import com.zving.framework.extend.action.AfterCronTaskExecutedAction;
import com.zving.framework.utility.DateUtil;
import com.zving.platform.bl.LogBL;
import com.zving.platform.log.type.CronLog;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class AfterCronTaskExecuted extends AfterCronTaskExecutedAction {
	private static long lastTime = 0;

	@Override
	public void execute(String taskManagerID, String taskID) {
		// 添加定时任务完成日志
		LogBL.addLog(CronLog.ID, CronLog.SubType_End, taskID + " finsh!");

		// 每隔3小时删除一下定时任务日志，最多保留7天的定时任务日志
		if (System.currentTimeMillis() - lastTime > 3 * 60 * 60 * 1000) {
			lastTime = System.currentTimeMillis();
			new Thread() {
				@Override
				public void run() {
					Date d = DateUtil.addDay(new Date(), -7);
					Q qb = new Q("delete from ZDUserLog where LogType=? and AddTime<?", CronLog.ID, d);
					qb.executeNoQuery();
				}
			}.start();
		}
	}

	@Override
	public boolean isUsable() {
		return true;
	}
}