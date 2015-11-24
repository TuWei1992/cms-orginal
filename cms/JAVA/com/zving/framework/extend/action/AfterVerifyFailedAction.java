package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * 数据校验失败后执行
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public abstract class AfterVerifyFailedAction implements IExtendAction {
	public static final String ID = "com.zving.framework.AfterPrivCheckFailedAction";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		String methodName = (String) args[0];
		String k = (String) args[1];
		String v = (String) args[2];
		String rule = (String) args[3];
		execute(methodName, k, v, rule);
		return null;
	}

	public abstract void execute(String methodName, String k, String v, String rule);

}
