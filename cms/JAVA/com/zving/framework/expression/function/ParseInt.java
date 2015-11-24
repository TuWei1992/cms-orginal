package com.zving.framework.expression.function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.utility.StringUtil;

/**
 * 将指定对象转换为整型。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-11
 */
public class ParseInt extends AbstractFunction {

	private static Pattern PNumber = Pattern.compile("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "ParseInt";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return new Class<?>[] { Object.class, String.class };
	}

	@Override
	public Object execute(IVariableResolver resolver, Object... args) throws ExpressionException {
		String value = String.valueOf(args[0]);
		Matcher matcher = PNumber.matcher(value);
		int changevalue = 0;
		if (matcher.find()) {
			String group0 = matcher.group(0);
			try {
				changevalue = Integer.parseInt(String.valueOf(value));
			} catch (NumberFormatException e) {
				if (group0.indexOf("+") != -1) {
					group0 = StringUtil.replaceEx(group0, "+", "");
				}
				/*
				 * if(group0.indexOf("-")!=-1){
				 * group0=StringUtil.replaceEx(group0,"-","");
				 * }
				 */
				if (group0.indexOf(".") != -1) {
					changevalue = Integer.parseInt(group0.substring(0, group0.indexOf(".")));
				}
			}
		}

		return String.valueOf(changevalue);
	}
}
