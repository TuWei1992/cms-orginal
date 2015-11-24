package com.zving.platform.point;

import com.zving.framework.UIFacade;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

public abstract class AfterGetNewMessage implements IExtendAction {

	public final static String ID = "com.zving.platform.AfterGetNewMessage";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		UIFacade ui = (UIFacade) args[0];
		execute(ui);
		return null;
	}

	public abstract void execute(UIFacade ui);

	@Override
	public boolean isUsable() {
		return true;
	}

}
