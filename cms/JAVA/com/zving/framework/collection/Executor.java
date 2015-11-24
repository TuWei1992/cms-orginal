package com.zving.framework.collection;

/**
 * 执行器,将要执行的JAVA逻辑传递给其他函数，让其他函数择机调用。<br>
 * 
 * @Author 王育春
 * @Date 2009-4-28
 * @Mail wyuch@zving.com
 */
public abstract class Executor {
	protected Object[] params;

	public Executor(Object... params) {
		this.params = params;
	}

	public abstract boolean execute();
}
