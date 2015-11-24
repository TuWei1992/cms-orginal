package com.zving.platform.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.action.ZhtmlContext;
import com.zving.framework.extend.action.ZhtmlExtendAction;

public class APIPriv extends ZhtmlExtendAction {
	@Override
	public void execute(ZhtmlContext context) throws ExtendException {
		String Type = context.getRequest().getString("Type");
		String ID = context.getRequest().getString("ID");
		context.include("platform/apiPrivExtend.zhtml?Type=" + Type + "&ID=" + ID);
	}

	@Override
	public boolean isUsable() {
		return true;
	}

}