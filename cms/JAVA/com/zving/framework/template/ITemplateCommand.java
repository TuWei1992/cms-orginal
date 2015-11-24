package com.zving.framework.template;

import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * 模板命令接口
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-9
 */
public interface ITemplateCommand {
	public int execute(AbstractExecuteContext context) throws TemplateRuntimeException;
}
