package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * 参数求值标签，一般不需要再使用，直接在zhtml中使用表达式即可。
 * 
 * @Author 王育春
 * @Date 2010-11-23
 * @Mail wyuch@zving.com
 */
public class ParamTag extends AbstractTag {
	private String var;

	private String Default;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "param";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		var = context.eval(var);
		if (var != null) {
			String v = String.valueOf(var);
			v = LangUtil.get(v);
			pageContext.getOut().write(v);
		} else if (Default != null) {
			pageContext.getOut().write(Default);
		}
		return SKIP_BODY;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public String getDefault() {
		return Default;
	}

	public void setDefault(String default1) {
		Default = default1;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("var"));
		list.add(new TagAttr("default"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.Tag.ParamTagName}";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

}
