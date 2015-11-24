package com.zving.platform.point;

import com.zving.framework.data.DBConn;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-12
 */
public abstract class AfterInstallAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.platform.AfterInstall";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		DBConn conn = (DBConn) args[0];
		execute(conn);
		return null;
	}

	public abstract void execute(DBConn conn) throws ExtendException;

}
