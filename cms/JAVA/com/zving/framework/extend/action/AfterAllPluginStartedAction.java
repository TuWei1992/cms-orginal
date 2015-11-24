package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * 在所有插件初始化完成后执行此动作
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-7-27
 */
public abstract class AfterAllPluginStartedAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.framework.AfterAllPluginStarted";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		execute();
		return null;
	}

	public abstract void execute();
}
