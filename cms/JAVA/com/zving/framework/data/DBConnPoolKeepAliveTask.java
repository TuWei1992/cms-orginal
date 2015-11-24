package com.zving.framework.data;

import com.zving.framework.schedule.SystemTask;

/**
 * 数据库连接保持活动任务,每三分钟执行一次
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-2-11
 */
public class DBConnPoolKeepAliveTask extends SystemTask {
	public static final String ID = "com.zving.framework.data.DBConnPoolKeepAliveTask";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.DBConnPoolKeepAliveTask}";
	}

	@Override
	public void execute() {
		if (DBConnPoolManager.poolMap == null) {
			return;
		}
		for (DBConnPool pool : DBConnPoolManager.poolMap.values()) {
			pool.keepAlive();
		}
	}

	@Override
	public String getDefaultCronExpression() {
		return "*/3 * * * *";
	}
}