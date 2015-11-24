package com.zving.framework.expression;

/**
 * 标签内的求值上下文
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-20
 */
public interface ITagData {
	/**
	 * @return 上级求值上下文
	 */
	public ITagData getParent();

	/**
	 * @param var 变量
	 * @return 变量对应的值
	 */
	public Object getValue(String var);

	/**
	 * 是否已经找到，在getValue()之后调用此方法来判断是否还要继续往上一级查找。<br>
	 */
	public boolean isFound();
}
