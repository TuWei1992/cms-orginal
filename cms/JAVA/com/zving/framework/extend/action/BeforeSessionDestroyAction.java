package com.zving.framework.extend.action;

import javax.servlet.http.HttpSession;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * Session销毁之前执行
 * 
 * @date 2009-11-7 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public abstract class BeforeSessionDestroyAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.framework.BeforeSessionDestory";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		HttpSession session = (HttpSession) args[0];
		execute(session);
		return null;
	}

	public abstract void execute(HttpSession session) throws ExtendException;
}
