package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.StringUtil;

/**
 * 将字符串进行XML转码
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class EscapeXml extends AbstractFunction {
	@Override
	public String getFunctionName() {
		return "escapeXml";
	}

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		if (input == null) {
			return "";
		}
		return StringUtil.htmlEncode(input);
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
