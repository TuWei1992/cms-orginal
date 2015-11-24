package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * UI方法调用之前执行
 * 
 * @date 2009-11-7 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public abstract class BeforeUIMethodInvokeAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.framework.BeforeUIMethodInvoke";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		execute((String) args[0]);
		return null;
	}

	public abstract void execute(String method) throws ExtendException;
}
