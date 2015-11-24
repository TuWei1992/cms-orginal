package com.zving.framework.template;

/**
 * 模板管理器接口
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-9-12
 */
public interface ITemplateManager {
	public TemplateExecutor getExecutor(String file);

	public boolean execute(String file, AbstractExecuteContext context);
}
