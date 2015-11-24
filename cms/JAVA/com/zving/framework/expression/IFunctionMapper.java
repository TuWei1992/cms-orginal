package com.zving.framework.expression;

/**
 * 函数查找器接口
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public interface IFunctionMapper {
	/**
	 * @param prefix 函数前缀
	 * @param name 函数名
	 * @return 表达式函数，如果没有找到则返回null
	 */
	public IFunction resolveFunction(String prefix, String name);

	/**
	 * 注册一个函数
	 * 
	 * @param function 待注册的函数
	 */
	public void registerFunction(IFunction function);
}
