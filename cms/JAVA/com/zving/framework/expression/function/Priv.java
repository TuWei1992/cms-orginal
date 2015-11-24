package com.zving.framework.expression.function;

import com.zving.framework.User;
import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.ObjectUtil;

/**
 * 判断当前用户是否拥有第一个参数指定的权限项
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Priv extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		if (ObjectUtil.empty(input)) {
			return false;
		}
		return User.getPrivilege().hasPriv(input);
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return AbstractFunction.Arg_String;
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "priv";
	}
}
