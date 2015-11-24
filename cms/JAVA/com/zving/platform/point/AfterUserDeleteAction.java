package com.zving.platform.point;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.framework.orm.DAOSet;
import com.zving.schema.ZDPrivilege;
import com.zving.schema.ZDUser;
import com.zving.schema.ZDUserRole;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-10
 */
public abstract class AfterUserDeleteAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.platform.AfterUserDelete";

	@Override
	@SuppressWarnings("unchecked")
	public Object execute(Object[] args) throws ExtendException {
		DAOSet<ZDUser> userSet = (DAOSet<ZDUser>) args[0];
		DAOSet<ZDUserRole> userRoleSet = (DAOSet<ZDUserRole>) args[1];
		DAOSet<ZDPrivilege> privSet = (DAOSet<ZDPrivilege>) args[2];
		execute(userSet, userRoleSet, privSet);
		return null;
	}

	public abstract void execute(DAOSet<ZDUser> userSet, DAOSet<ZDUserRole> userRoleSet, DAOSet<ZDPrivilege> privSet)
			throws ExtendException;
}