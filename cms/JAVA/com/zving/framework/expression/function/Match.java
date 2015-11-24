package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;

/**
 * 第一个参数是否正则匹配第二个参数
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Match extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		String regex = (String) args[1];
		if (input == null) {
			input = "";
		}
		if (regex == null) {
			return false;
		}
		return input.matches(regex);
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
		return "match";
	}
}
