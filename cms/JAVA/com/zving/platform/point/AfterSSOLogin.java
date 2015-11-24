package com.zving.platform.point;

import com.zving.framework.UIFacade;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-12-6
 */
public abstract class AfterSSOLogin implements IExtendAction {
	public static final String ExtendPointID = "com.zving.platform.AfterSSOLogin";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		UIFacade ui = (UIFacade) args[0];
		execute(ui);
		return null;
	}

	public abstract void execute(UIFacade ui);
}
