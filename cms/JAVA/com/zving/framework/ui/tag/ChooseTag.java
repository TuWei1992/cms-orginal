package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;

/**
 * <z:choose>标签,用于实现Choose/When支持
 * 
 * @Author 王育春
 * @Date 2010-12-15
 * @Mail wyuch@zving.com
 */
public class ChooseTag extends AbstractTag {

	private Object variable;

	private boolean matched;// 是否有when标签匹配中了

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "choose";
	}

	public Object getVariable() {
		return variable;
	}

	public void setVariable(Object variable) {
		this.variable = variable;
	}

	public boolean isMatched() {
		return matched;
	}

	public void setMatched(boolean matched) {
		this.matched = matched;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("variable", true));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ChooseTag.Name}";
	}

	@Override
	public String getDescription() {
		return null;
	}

}
