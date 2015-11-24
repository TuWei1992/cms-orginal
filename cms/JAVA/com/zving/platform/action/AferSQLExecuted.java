package com.zving.platform.action;

import com.zving.framework.extend.action.AfterSQLExecutedAction;
import com.zving.platform.bl.LogBL;
import com.zving.platform.config.SlowSQLThreshold;
import com.zving.platform.log.type.SQLLog;

/**
 * 将慢SQL和SQL执行错误信息写入数据库
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class AferSQLExecuted extends AfterSQLExecutedAction {
	@Override
	public void execute(long costTime, String message) {
		String subType = null;
		if (message.startsWith("Error:")) {
			subType = SQLLog.SubType_Error;
		} else if (message.startsWith("Timeout:")) {
			subType = SQLLog.SubType_Timeout;
		} else if (costTime >= SlowSQLThreshold.getValue()) {
			subType = SQLLog.SubType_Slow;
		} else {
			return;
		}
		LogBL.addLog(SQLLog.ID, subType, "CostTime=" + costTime + ",Message=" + message);
	}

	@Override
	public boolean isUsable() {
		return true;
	}
}