package com.zving.platform.point;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.schema.ZDRole;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-11
 */
public abstract class AfterRoleAddAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.platform.AfterRoleAdd";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		ZDRole role = (ZDRole) args[0];
		execute(role);
		return null;
	}

	public abstract void execute(ZDRole role) throws ExtendException;
}