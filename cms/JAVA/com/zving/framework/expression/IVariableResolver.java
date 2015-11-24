package com.zving.framework.expression;

/**
 * 变量查找器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public interface IVariableResolver {
	/**
	 * @param varName 变量名称
	 * @return 查找到的变量值，如果未找到则返回null
	 * @throws ExpressionException
	 */
	public Object resolveVariable(String varName) throws ExpressionException;
}
