package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.StringUtil;

/**
 * 字符串替换，如果指定了第四个参数且其值为"regex"则使用正则替换。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Replace extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		String src = (String) args[1];
		String dest = (String) args[2];
		String type = "";
		if (args.length > 3) {
			type = (String) args[3];
		}
		if (input == null) {
			input = "";
		}
		if (src == null) {
			return input;
		}
		if (dest == null) {
			dest = "";
		}
		if ("regex".equalsIgnoreCase(type)) {
			return input.replaceAll(src, dest);
		} else {
			return StringUtil.replaceEx(input, src, dest);
		}
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return new Class<?>[] { String.class, String.class, String.class, String.class };
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "replace";
	}
}
