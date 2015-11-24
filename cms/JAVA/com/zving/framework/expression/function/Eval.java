package com.zving.framework.expression.function;

import java.util.Map;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.CachedEvaluator;
import com.zving.framework.expression.DefaultFunctionMapper;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.expression.IVariableResolver;
import com.zving.framework.expression.MapVariableResolver;
import com.zving.framework.template.AbstractExecuteContext;

/**
 * 执行一段含有表达式的字符串
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public class Eval extends AbstractFunction {

	@Override
	public Object execute(IVariableResolver resolver, Object... args) throws ExpressionException {
		String input = (String) args[0];
		if (!(resolver instanceof AbstractExecuteContext)) {
			return input;
		}
		AbstractExecuteContext context = (AbstractExecuteContext) resolver;
		return context.evalExpression(input);
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return AbstractFunction.Arg_String;
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public String getFunctionName() {
		return "eval";
	}

	public static Object eval(String expression, Map<?, ?> map) throws ExpressionException {
		MapVariableResolver vr = new MapVariableResolver(map);
		CachedEvaluator ce = new CachedEvaluator();
		return ce.evaluate(expression, Object.class, vr, DefaultFunctionMapper.getInstance());
	}
}
