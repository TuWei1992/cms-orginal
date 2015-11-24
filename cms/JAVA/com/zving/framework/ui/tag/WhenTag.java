package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.UIException;
import com.zving.framework.utility.Operators;
import com.zving.framework.utility.Primitives;
import com.zving.framework.utility.StringUtil;

/**
 * <z:when>标签,用于实现Choose/When支持
 * 
 * @Author 王育春
 * @Date 2010-12-15
 * @Mail wyuch@zving.com
 */
public class WhenTag extends AbstractTag {

	private String value;

	private String out;

	private boolean other;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "when";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		AbstractTag tag = getParent();
		if (!(tag instanceof ChooseTag)) {
			throw new UIException("tag when must in tag choose");
		}
		ChooseTag parent = (ChooseTag) tag;
		Object v1 = parent.getVariable();
		Object v2 = value;
		if (!other) {
			if (value == null) {
				throw new UIException("tag when's other and value can't be empty at the same time");
			}
		}
		if (other) {
			if (!parent.isMatched()) {
				output(parent);
				return EVAL_BODY_INCLUDE;
			}
			return SKIP_BODY;
		} else if (Primitives.getBoolean(Operators.eq(v1, v2))) {
			output(parent);
			return EVAL_BODY_INCLUDE;
		} else {
			return SKIP_BODY;
		}
	}

	private void output(ChooseTag parent) {
		if (StringUtil.isNotEmpty(out)) {
			pageContext.getOut().write(out);
		}
		parent.setMatched(true);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public boolean isOther() {
		return other;
	}

	public void setOther(boolean other) {
		this.other = other;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("value"));
		list.add(new TagAttr("out"));
		list.add(new TagAttr("other", TagAttr.BOOL_OPTIONS));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.WhenTag.Name}";
	}

	@Override
	public String getDescription() {
		return null;
	}

}
