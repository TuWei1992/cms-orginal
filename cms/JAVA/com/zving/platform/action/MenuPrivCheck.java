package com.zving.platform.action;

import com.zving.framework.User;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.action.PrivExtendAction;
import com.zving.framework.security.Privilege;
import com.zving.platform.config.AdminUserName;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-16
 */
public class MenuPrivCheck extends PrivExtendAction {
	@Override
	public int getPrivFlag(String priv) throws ExtendException {
		if (AdminUserName.getValue().equals(User.getUserName())) {
			return Privilege.Flag_Allow;
		}
		if (User.getPrivilege().hasPriv(priv)) {
			return Privilege.Flag_Allow;
		} else {
			return Privilege.Flag_NotSet;
		}
	}

	@Override
	public boolean isUsable() {
		return true;
	}
}