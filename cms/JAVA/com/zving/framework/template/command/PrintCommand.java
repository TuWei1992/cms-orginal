package com.zving.framework.template.command;

import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.ITemplateCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * 原文输出命令
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-9
 */
public class PrintCommand implements ITemplateCommand {
	private String str;

	public PrintCommand(String str) {
		this.str = str;
	}

	@Override
	public int execute(AbstractExecuteContext context) throws TemplateRuntimeException {
		context.getOut().write(str);
		return AbstractTag.EVAL_PAGE;
	}

}
