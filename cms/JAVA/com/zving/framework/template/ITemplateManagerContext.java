package com.zving.framework.template;

import java.util.List;

import com.zving.framework.expression.IEvaluator;
import com.zving.framework.expression.IFunctionMapper;

/**
 * 模板管理上下文接口，用于提供标签/模板类型/修饰符的注册入口，并管理模板路径。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-10-27
 */
public interface ITemplateManagerContext {

	/**
	 * 返回所有源代码处理器
	 */
	public List<ITemplateSourceProcessor> getSourceProcessors();

	/**
	 * 返回所有标签
	 */
	public List<? extends AbstractTag> getTags();

	/**
	 * 返回指定ID的标签
	 */
	public AbstractTag getTag(String prefix, String tagName);

	/**
	 * 返回指定标签的新实例
	 */
	public AbstractTag createNewTagInstance(String prefix, String tagName);

	/**
	 * @return 获取模板管理器
	 */
	public ITemplateManager getTemplateManager();

	/**
	 * 返回IFunctionMapper实例
	 */
	public abstract IFunctionMapper getFunctionMapper();

	/**
	 * 返回表达式求值器实例
	 */
	public abstract IEvaluator getEvaluator();
}
