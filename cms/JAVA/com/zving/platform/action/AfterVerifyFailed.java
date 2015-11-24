package com.zving.platform.action;

import com.zving.framework.extend.action.AfterVerifyFailedAction;
import com.zving.framework.utility.StringFormat;
import com.zving.platform.bl.LogBL;
import com.zving.platform.log.type.SecurityLog;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class AfterVerifyFailed extends AfterVerifyFailedAction {

	@Override
	public void execute(String methodName, String k, String v, String rule) {
		String str = "Method=?,Key=?,Value=?,Rule=?";
		String message = StringFormat.format(str, methodName, k, v, rule);
		LogBL.addLog(SecurityLog.ID, SecurityLog.SubType_Verify, message);
	}

	@Override
	public boolean isUsable() {
		return true;
	}
}