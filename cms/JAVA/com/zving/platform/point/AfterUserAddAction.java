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
public abstract class AfterUserAddAction implements IExtendAction {
	public static String ExtendPointID = "com.zving.platform.AfterUserAdd";

	@Override
	@SuppressWarnings("unchecked")
	public Object execute(Object[] args) throws ExtendException {
		ZDUser user = (ZDUser) args[0];
		ZDPrivilege priv = (ZDPrivilege) args[1];
		DAOSet<ZDUserRole> set = (DAOSet<ZDUserRole>) args[2];
		execute(user, priv, set);
		return null;
	}

	public abstract void execute(ZDUser user, ZDPrivilege priv, DAOSet<ZDUserRole> userRoleSet) throws ExtendException;
}