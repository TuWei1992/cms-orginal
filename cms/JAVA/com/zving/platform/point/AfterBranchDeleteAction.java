package com.zving.platform.point;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-10
 */
public abstract class AfterBranchDeleteAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.Platform.AfterBranchDelete";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		String[] ids = (String[]) args[0];
		execute(ids);
		return null;
	}

	public abstract void execute(String[] ids);
}