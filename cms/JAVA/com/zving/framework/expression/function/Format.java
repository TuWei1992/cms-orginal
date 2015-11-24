package com.zving.framework.expression.function;

import java.text.DecimalFormat;
import java.util.Date;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.DateUtil;

/**
 * 按第二个参数指定的格式格式化第一个参数<br>
 * 如果不指定格式则直接输出.
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Format extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		Object value = args[0];
		String format = null;
		if (args.length > 1) {
			format = (String) args[1];
		}
		if (format != null) {
			if (value instanceof Date) {
				return DateUtil.toString((Date) value, format);
			} else if (value instanceof String && DateUtil.isDateTime((String) value)) {
				return DateUtil.toString(DateUtil.parseDateTime((String) value), format);
			} else if (value instanceof Number) {
				return new DecimalFormat(format).format(value);
			}
		}
		return value;
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return new Class<?>[] { Object.class, String.class };
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "format";
	}
}
