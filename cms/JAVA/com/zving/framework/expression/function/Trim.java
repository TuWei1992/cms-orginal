package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;

/**
 * 清除掉字符串前后的空格。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Trim extends AbstractFunction {
	@Override
	public String getFunctionName() {
		return "trim";
	}

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		if (input == null) {
			return "";
		}
		return input.trim();
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return AbstractFunction.Arg_String;
	}
}
