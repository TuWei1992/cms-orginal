package com.zving.framework.expression;

import com.zving.framework.extend.IExtendItem;

/**
 * 表达式函数接口
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public interface IFunction extends IExtendItem {
	/**
	 * 指定函数前缀。如果有前缀，则使用函数时类似于${prefix:func(a,b)};<br>
	 * 如果前缀为空或者null，则使用函数时类似于${func(a,b)}。
	 */
	public String getFunctionPrefix();

	/**
	 * 指定函数名
	 */
	public String getFunctionName();

	/**
	 * 指定函数的参数，模板机制会将传入的参数转换成你指定的类型
	 */
	public Class<?>[] getArgumentTypes();

	/**
	 * 返回执行结果。其中args为传入的参数数组
	 */
	public Object execute(IVariableResolver resolver, Object... args) throws ExpressionException;
}
