package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;

/**
 * 判断第一个参数是否以第二个参数开始。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class StartsWith extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		String substring = (String) args[1];
		if (input == null) {
			input = "";
		}
		if (substring == null) {
			substring = "";
		}
		return input.startsWith(substring);
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return AbstractFunction.Arg_String_String;
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "startsWith";
	}
}
