package com.zving.framework.expression;

/**
 * 表达式执行器接口
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-23
 */
public interface IEvaluator {
	/**
	 * 执行表达式
	 * 
	 * @param expression 表达式
	 * @param expectedType 期望的执行结果类型
	 * @param resolver 变量查找器
	 * @param functionMapper 函数查找器
	 * @return 表达式执行结果
	 * @throws ExpressionException
	 */
	public Object evaluate(String expression, Class<?> expectedType, IVariableResolver resolver, IFunctionMapper functionMapper)
			throws ExpressionException;

}
