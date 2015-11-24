package com.zving.platform.point;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.schema.ZDBranch;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-10
 */
public abstract class AfterBranchAddAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.Platform.AfterBranchAdd";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		ZDBranch branch = (ZDBranch) args[0];
		execute(branch);
		return null;
	}

	public abstract void execute(ZDBranch branch) throws ExtendException;
}
