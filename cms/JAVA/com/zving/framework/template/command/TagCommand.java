package com.zving.framework.template.command;

import java.util.List;

import com.zving.framework.expression.ExpressionException;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.ITemplateCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * 标签执行命令
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-9
 */
public class TagCommand implements ITemplateCommand {
	private AbstractTag tag;
	private ITemplateCommand[] commands;
	private boolean hasBody;
	private int depth;

	public TagCommand(AbstractTag tag, List<ITemplateCommand> commandList, int depth, boolean hasBody) {
		this.tag = tag;
		commands = new ITemplateCommand[commandList.size()];
		commands = commandList.toArray(commands);
		this.hasBody = hasBody;
		this.depth = depth;
	}

	@Override
	public int execute(AbstractExecuteContext context) throws TemplateRuntimeException {
		AbstractTag tagCopy = (AbstractTag) tag.clone();
		AbstractTag parent = context.getCurrentTag();
		tagCopy.setPageContext(context);
		tagCopy.setParent(parent);
		context.setCurrentTag(tagCopy);
		try {
			tagCopy.init();
		} catch (ExpressionException e) {
			throw new TemplateRuntimeException(e.getMessage());
		}
		int startFlag = tagCopy.doStartTag();
		if (startFlag == AbstractTag.SKIP_PAGE) {
			return startFlag;
		}
		boolean bufferFlag = false;
		try {
			if (startFlag != AbstractTag.SKIP_BODY && hasBody) {
				if (startFlag != AbstractTag.EVAL_BODY_INCLUDE) {
					context.getOut().beginBuffer();// 开始缓冲区
					bufferFlag = true;
				}
				while (true) {
					for (ITemplateCommand command : commands) {
						if (command.execute(context) == AbstractTag.SKIP_PAGE) {
							break;
						}
					}
					if (tagCopy.doAfterBody() == AbstractTag.EVAL_BODY_AGAIN) {
						if (bufferFlag) {
							context.getOut().commitBuffer();
							context.getOut().beginBuffer();
						}
					} else {
						break;
					}
				}
			}
			int endFlag = tagCopy.doEndTag();
			context.setCurrentTag(parent);
			if (endFlag == AbstractTag.SKIP_PAGE) {
				return AbstractTag.SKIP_PAGE;
			}
			return AbstractTag.EVAL_PAGE;
		} finally {
			if (bufferFlag) {
				context.getOut().commitBuffer();// 如果有开辟缓冲区则必须合并，否则后面的所有标签都不会再输出了
			}
		}
	}

	public AbstractTag getTag() {
		return tag;
	}

	public void setTag(AbstractTag tag) {
		this.tag = tag;
	}

	public ITemplateCommand[] getCommands() {
		return commands;
	}

	public void setCommands(ITemplateCommand[] commands) {
		this.commands = commands;
	}

	public boolean isHasBody() {
		return hasBody;
	}

	public void setHasBody(boolean hasBody) {
		this.hasBody = hasBody;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}
