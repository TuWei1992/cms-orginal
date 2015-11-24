package com.zving.framework.extend.action;

import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;

/**
 * 权限检查扩展行为虚拟类。<br>
 * 各个插件通过注册权限检查扩展行为实现自定义的权限检查逻辑。
 * 
 * @Author 王育春
 * @Date 2011-1-18
 * @Mail wyuch@zving.com
 */
public abstract class PrivExtendAction implements IExtendAction {
	public static String ExtendPointID = "com.zving.framework.PrivCheck";

	@Override
	public Object execute(Object[] args) throws ExtendException {
		return getPrivFlag((String) args[0]);
	}

	public abstract int getPrivFlag(String priv) throws ExtendException;
}
