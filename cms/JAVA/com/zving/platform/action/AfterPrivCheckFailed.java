package com.zving.platform.action;

import com.zving.framework.Member;
import com.zving.framework.User;
import com.zving.framework.extend.action.AfterPrivCheckFailedAction;
import com.zving.framework.utility.StringFormat;
import com.zving.platform.bl.LogBL;
import com.zving.platform.log.type.SecurityLog;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class AfterPrivCheckFailed extends AfterPrivCheckFailedAction {

	@Override
	public void execute(String message) {
		String str = "Login=?,LoginType=?,UserType=?,Message=?";
		String loginType = "-";
		if (User.isLogin()) {
			loginType = "User";
		} else if (Member.isLogin()) {
			loginType = "Member";
		} else {
			return;
		}
		message = StringFormat.format(str, User.isLogin(), loginType, User.getType(), message);
		LogBL.addLog(SecurityLog.ID, SecurityLog.SubType_PrivCheck, message);
	}

	@Override
	public boolean isUsable() {
		return true;
	}
}