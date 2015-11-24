package com.zving.framework.expression;

/**
 * 表达式函数虚拟类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-22
 */
public abstract class AbstractFunction implements IFunction {
	/**
	 * 对象参数
	 */
	public static final Class<?>[] Arg_Object = new Class<?>[] { Object.class };
	/**
	 * 字符串参数
	 */
	public static final Class<?>[] Arg_String = new Class<?>[] { String.class };
	/**
	 * 字符串-字符串双参数
	 */
	public static final Class<?>[] Arg_String_String = new Class<?>[] { String.class, String.class };
	/**
	 * 字符串-整型-整型三参数
	 */
	public static final Class<?>[] Arg_String_Int_Int = new Class<?>[] { String.class, Integer.class, Integer.class };
	/**
	 * 字符串-整型参数
	 */
	public static final Class<?>[] Arg_String_Int = new Class<?>[] { String.class, Integer.class };

	@Override
	public String getExtendItemID() {
		String prefix = getFunctionPrefix();
		return (prefix == null ? "" : prefix) + ":" + getFunctionName();
	}

	@Override
	public String getExtendItemName() {
		return getExtendItemID();
	}
}
