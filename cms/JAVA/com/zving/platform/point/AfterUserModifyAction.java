package com.zving.platform.point;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.framework.orm.DAOSet;
import com.zving.schema.ZDUser;
import com.zving.schema.ZDUserRole;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-10
 */
public abstract class AfterUserModifyAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.platform.AfterUserModify";

	@Override
	@SuppressWarnings("unchecked")
	public Object execute(Object[] args) throws ExtendException {
		ZDUser user = (ZDUser) args[0];
		DAOSet<ZDUserRole> oldUserRole = (DAOSet<ZDUserRole>) args[1];
		String newRoleCodes = (String) args[2];
		execute(user, oldUserRole, newRoleCodes);
		return null;
	}

	public abstract void execute(ZDUser user, DAOSet<ZDUserRole> userRoleSet, String newRoleCodes) throws ExtendException;
}
