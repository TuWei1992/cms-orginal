package com.zving.framework.template.command;

import java.util.Date;

import com.zving.framework.config.ExpressionAutoEscaping;
import com.zving.framework.expression.ExpressionException;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.ITemplateCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 表达式命令
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-9
 */
public class ExpressionCommand implements ITemplateCommand {
	private String expr;
	private String source;
	private boolean autoEscaping = true;
	private boolean i18N = false;

	public ExpressionCommand(String str) {
		source = expr = str;
		if (expr.startsWith("@")) {// 国际化字符串
			i18N = true;
		}
		if (expr.startsWith("${(") && expr.endsWith(")}")) {
			autoEscaping = false;// 保持原样输出
			expr = "${" + expr.substring(3, expr.length() - 2) + "}";
		} else if (!ExpressionAutoEscaping.getValue()) {
			autoEscaping = false;
		}
	}

	@Override
	public int execute(AbstractExecuteContext context) throws TemplateRuntimeException {
		try {
			Object v = "";
			if (i18N) {
				v = LangUtil.get(expr, context.getLanguage());
			} else {
				v = context.evalExpression(expr);
				if (v == null) {
					v = "";
				} else {
					if (v instanceof String) {
						String str = LangUtil.get(v.toString(), context.getLanguage());
						if (autoEscaping) {
							v = StringUtil.quickHtmlEncode(str);
						} else {
							v = str;
						}
					} else if (v instanceof Date) {
						v = DateUtil.toDateTimeString((Date) v);
					}
				}
			}
			context.getOut().write(v);
		} catch (ExpressionException e) {
			throw new TemplateRuntimeException(e);
		}
		return AbstractTag.EVAL_PAGE;
	}

	public String getSource() {
		return source;
	}
}
