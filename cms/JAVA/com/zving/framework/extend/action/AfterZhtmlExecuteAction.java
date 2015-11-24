package com.zving.framework.extend.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * Zhtml页面执行后执行
 * 
 * @date 2009-11-7 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public abstract class AfterZhtmlExecuteAction implements IExtendAction {
	public static final String ExtendPointID = "com.zving.framework.AfterZhtmlExecuteAction";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		HttpServletRequest request = (HttpServletRequest) args[0];
		HttpServletResponse response = (HttpServletResponse) args[1];
		execute(request, response);
		return null;
	}

	public abstract void execute(HttpServletRequest request, HttpServletResponse response) throws ExtendException;
}
