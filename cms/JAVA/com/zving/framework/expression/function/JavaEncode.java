package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.StringUtil;

/**
 * 对字符串进行java转码，以便于在javascript中输出成字符串。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-13
 */
public class JavaEncode extends AbstractFunction {
	@Override
	public String getFunctionName() {
		return "javaEncode";
	}

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		if (input == null) {
			return "";
		}
		return StringUtil.javaEncode(input);
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
