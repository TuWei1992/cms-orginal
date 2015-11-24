package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.Primitives;

/**
 * if条件分支标签.<br>
 * 
 * @Author 王育春
 * @Date 2010-11-19
 * @Mail wyuch@zving.com
 */
public class IfTag extends AbstractTag {

	public static final String IfTagInAttribute = "_ZVING_IF_TAG";

	protected String condition;

	protected String out;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "if";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		if (parent == null) {
			pageContext.addRootVariable(IfTagInAttribute, this);
		} else {
			parent.getVariables().put(IfTagInAttribute, this);
		}
		if (Primitives.getBoolean(condition)) {
			if (out != null) {
				pageContext.getOut().write(out);
			}
			return EVAL_BODY_INCLUDE;
		} else {
			return SKIP_BODY;
		}
	}

	public boolean isPass() {
		return Primitives.getBoolean(condition);
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getTest() {// 兼容jstl的写法
		return condition;
	}

	public void setTest(String condition) {
		this.condition = condition;
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
		list.add(new TagAttr("condition", DataTypes.STRING, "@{Framework.IfTag.Condition}"));
		list.add(new TagAttr("out", DataTypes.STRING, "@{Framework.IfTag.Output}"));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZIfTagDescription}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ZIfTagName}";
	}

}
