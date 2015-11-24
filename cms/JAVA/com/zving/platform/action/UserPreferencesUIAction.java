package com.zving.platform.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.action.ZhtmlContext;
import com.zving.framework.extend.action.ZhtmlExtendAction;

public class UserPreferencesUIAction extends ZhtmlExtendAction {

	@Override
	public boolean isUsable() {
		return true;
	}

	@Override
	public void execute(ZhtmlContext context) throws ExtendException {
		context.include("platform/userPreferencesExtend.zhtml");
	}

}
