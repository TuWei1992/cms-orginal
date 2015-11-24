package com.zving.framework.expression.function;

import java.util.Date;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IVariableResolver;

/**
 * 返回表示当前时间的Date对象。
 * 用法：${now().time}返回毫秒数,${format(now(),'yyyy-MM-dd')}返回当前日期
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Now extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) throws ExpressionException {
		return new Date();
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return new Class<?>[] {};
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "now";
	}
}
