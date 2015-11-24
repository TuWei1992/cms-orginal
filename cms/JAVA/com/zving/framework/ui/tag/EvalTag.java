package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * 表达式求值标签，一般不需要使用本标签，可以直接在zhtml中使用表达式。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-8
 */
public class EvalTag extends ParamTag {
	String expression;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "eval";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		try {
			Object value = expression;// 表达式已经改为自动求值
			if (value != null) {
				String v = String.valueOf(value);
				v = LangUtil.get(v);
				pageContext.getOut().write(v);
			} else {
				pageContext.getOut().write("null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("expression"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.Tag.EvalTagName}";
	}

}
