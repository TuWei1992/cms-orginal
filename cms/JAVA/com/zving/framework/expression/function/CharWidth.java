package com.zving.framework.expression.function;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.StringUtil;

/**
 * 截取指定宽度的字符，其中宽度算法为：ASCII字符算1个宽度，非ASCII字符算2个宽度。<br>
 * 可以指事实第三个参数，用来在发生截取时加上后缀（一般是省略号），如果未发生截取（指定宽度大于字符串宽度）则不加后缀。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class CharWidth extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String input = (String) args[0];
		Integer charWidth = 0;
		if (args.length > 1) {
			charWidth = (Integer) args[1];
		}
		String suffix = "";
		if (args.length > 2) {
			suffix = (String) args[2];
		}
		String r = StringUtil.quickHtmlDecode(input);
		boolean decoded = r.length() != input.length();
		if (StringUtil.lengthEx(r) > charWidth) {
			r = StringUtil.subStringEx(r, charWidth);
			if (decoded) {
				r = StringUtil.quickHtmlEncode(r);
			}
			return r + suffix;
		}
		return input;

	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "charWidth";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return new Class<?>[] { String.class, Integer.class, String.class };
	}

}
