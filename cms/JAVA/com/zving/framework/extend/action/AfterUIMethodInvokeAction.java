package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * UI方法调用后执行
 * 
 * @date 2009-11-7 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public abstract class AfterUIMethodInvokeAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.framework.AfterUIMethodInvoke";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		execute((String) args[0]);
		return null;
	}

	public abstract void execute(String method) throws ExtendException;
}
