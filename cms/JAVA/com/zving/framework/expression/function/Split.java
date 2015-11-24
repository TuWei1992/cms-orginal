package com.zving.framework.expression.function;

import java.util.StringTokenizer;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;

/**
 * 将第一个参数按第二个参数分隔成数组。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Split extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		String delimiters = (String) args[1];
		String[] array;
		if (input == null) {
			input = "";
		}
		if (input.length() == 0) {
			array = new String[1];
			array[0] = "";
			return array;
		}

		if (delimiters == null) {
			delimiters = "";
		}

		StringTokenizer tok = new StringTokenizer(input, delimiters);
		int count = tok.countTokens();
		array = new String[count];
		int i = 0;
		while (tok.hasMoreTokens()) {
			array[i++] = tok.nextToken();
		}
		return array;
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
		return "split";
	}
}
