package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * Else分支标签
 * 
 * @Author 王育春
 * @Date 2010-11-20
 * @Mail wyuch@zving.com
 */
public class ElseTag extends AbstractTag {

	private String out;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "else";
	}

	protected static boolean isSkip(AbstractTag current, AbstractExecuteContext pageContext) {
		AbstractTag tag = null;
		if (current.getParent() == null) {
			tag = (AbstractTag) pageContext.getRootVariable(IfTag.IfTagInAttribute);
			pageContext.removeRootVariable(IfTag.IfTagInAttribute);

		} else {
			tag = (AbstractTag) current.getParent().getVariable(IfTag.IfTagInAttribute);
			current.getParent().getVariables().remove(IfTag.IfTagInAttribute);
		}
		if (tag == null || tag.getParent() != current.getParent()) {
			return true;
		}
		if (((IfTag) tag).isPass()) {// 如果if成立，则else不成立
			return true;
		}
		return false;
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		if (isSkip(this, pageContext)) {
			return SKIP_BODY;
		}
		if (out != null) {
			pageContext.getOut().write(out);
		}
		return EVAL_BODY_INCLUDE;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("out"));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZElseTagDescription}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ZElseTagName}";
	}

}
