package com.zving.framework.core.scanner;

import com.zving.framework.extend.IExtendItem;
import com.zving.framework.thirdparty.asm.tree.ClassNode;

/**
 * 编译后资源遍历器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-6
 */
public interface IBuiltResourceVisitor extends IExtendItem {
	/**
	 * @param br 编译后资源实例
	 * @return 本遍历器是否处理br对应的资源
	 */
	public boolean match(BuiltResource br);

	/**
	 * 遍历类型
	 * 
	 * @param br 编译后资源实例
	 * @param cn 类型节点
	 */
	public void visitClass(BuiltResource br, ClassNode cn);

	/**
	 * 遍历内部类
	 * 
	 * @param br 编译后资源实例
	 * @param outerClass 外部类节点
	 * @param innerClass 内部类节点
	 */
	public void visitInnerClass(BuiltResource br, ClassNode outerClass, ClassNode innerClass);

	/**
	 * 遍历非class资源
	 * 
	 * @param br 编译后资源实例
	 */
	public void visitResource(BuiltResource br);
}
