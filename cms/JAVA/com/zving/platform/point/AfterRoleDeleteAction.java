package com.zving.platform.point;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.framework.orm.DAOSet;
import com.zving.schema.ZDPrivilege;
import com.zving.schema.ZDRole;
import com.zving.schema.ZDUserRole;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-11
 */
public abstract class AfterRoleDeleteAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.platform.AfterRoleDelete";

	@Override
	@SuppressWarnings("unchecked")
	public Object execute(Object[] args) throws ExtendException {
		ZDRole role = (ZDRole) args[0];
		DAOSet<ZDUserRole> userRoleSet = (DAOSet<ZDUserRole>) args[1];// 用户角色集合
		DAOSet<ZDPrivilege> privSet = (DAOSet<ZDPrivilege>) args[2];// 权限集合
		execute(role, userRoleSet, privSet);
		return null;
	}

	public abstract void execute(ZDRole role, DAOSet<ZDUserRole> userRoleSet, DAOSet<ZDPrivilege> privSet) throws ExtendException;
}