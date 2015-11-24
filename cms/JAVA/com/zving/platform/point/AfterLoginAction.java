package com.zving.platform.point;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.schema.ZDUser;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-12-6
 */
public abstract class AfterLoginAction implements IExtendAction {
	public static final String ID = "com.zving.platform.AfterLogin";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		ZDUser user = (ZDUser) args[0];
		execute(user);
		return null;
	}

	public abstract void execute(ZDUser user);
}
