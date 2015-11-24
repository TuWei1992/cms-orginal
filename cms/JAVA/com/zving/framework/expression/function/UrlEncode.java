package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.StringUtil;

/**
 * 对字符串进行Url转码，以便于在Url中输出成字符串。
 * 
 * @author 甘明
 * @mail gm@zving.com
 * @date 2014-3-13
 */
public class UrlEncode extends AbstractFunction {
	@Override
	public String getFunctionName() {
		return "urlEncode";
	}

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		if (input == null) {
			return "";
		}
		if (args.length > 1) {
			String charset = (String) args[1];
			if (StringUtil.isNotEmpty(charset)) {
				return StringUtil.urlEncode(input, charset);
			}
		}
		return StringUtil.urlEncode(input);
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return new Class<?>[] { String.class, String.class };
	}
}
