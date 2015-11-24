package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.StringUtil;

/**
 * 截取字符串，如果第三个参数未指定，则截取第二个参数开始直到字符串的最后。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Substring extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		Integer start = (Integer) args[1];
		Integer end = null;
		if (args.length > 2) {
			end = (Integer) args[2];
		}
		if (input == null) {
			input = "";
		}
		input = StringUtil.htmlDecode(input); // 先解码再截取字符串
		if (end != null) {
			return input.substring(start, end);
		} else {
			return input.substring(start);
		}
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "substring";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return AbstractFunction.Arg_String_Int_Int;
	}

}
