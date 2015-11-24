package com.zving.framework.i18n;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.ObjectUtil;

/**
 * 国际化标签 ，用于在zhtml中根据当前语言显示一个国际化字符串
 * 
 * @Author 王育春
 * @Date 2011-4-13
 * @Mail wyuch@zving.com
 */
public class LangTag extends AbstractTag {

	private String id;

	private String Default;

	private String language;

	private String oldLanguage;

	private Object arg0;
	private Object arg1;
	private Object arg2;
	private Object arg3;
	private Object arg4;

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		try {
			if (id != null) {
				String str = getValue();
				if (ObjectUtil.empty(str)) {
					str = Default;
				}
				if (str == null) {
					str = "@{" + id + "}";
				}
				pageContext.getOut().write(str);
				return SKIP_BODY;
			}
			if (language != null) {
				oldLanguage = context.getLanguage();
				context.setLanguage(language);
				return EVAL_BODY_INCLUDE;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	private String getValue() {
		if (arg0 != null || arg1 != null || arg2 != null || arg3 != null || arg4 != null) {
			return LangMapping.get(id, new Object[] { arg0, arg1, arg2, arg3, arg4 });
		} else {
			return LangMapping.get(id);
		}
	}

	@Override
	public int doAfterBody() throws TemplateRuntimeException {
		if (oldLanguage != null) {
			context.setLanguage(oldLanguage);
		}
		return EVAL_PAGE;
	}

	public void setId(String var) {
		id = var;
	}

	public String getDefault() {
		return Default;
	}

	public void setDefault(String default1) {
		Default = default1;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "lang";
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", DataTypes.STRING, "@{Framework.LangTag.ID}"));
		list.add(new TagAttr("language", DataTypes.STRING, "@{Framework.LangTag.Language}"));
		list.add(new TagAttr("default", DataTypes.STRING, "@{Framework.LangTag.Default}"));
		list.add(new TagAttr("arg0", DataTypes.STRING, "@{Framework.LangTag.Arg}"));
		list.add(new TagAttr("arg1", DataTypes.STRING, "@{Framework.LangTag.Arg}"));
		list.add(new TagAttr("arg2", DataTypes.STRING, "@{Framework.LangTag.Arg}"));
		list.add(new TagAttr("arg3", DataTypes.STRING, "@{Framework.LangTag.Arg}"));
		list.add(new TagAttr("arg4", DataTypes.STRING, "@{Framework.LangTag.Arg}"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.LangTag.Name}";
	}

	@Override
	public String getDescription() {
		return "@{Framework.LangTag.Desc}";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	public Object getArg0() {
		return arg0;
	}

	public void setArg0(Object arg0) {
		this.arg0 = arg0;
	}

	public Object getArg1() {
		return arg1;
	}

	public void setArg1(Object arg1) {
		this.arg1 = arg1;
	}

	public Object getArg2() {
		return arg2;
	}

	public void setArg2(Object arg2) {
		this.arg2 = arg2;
	}

	public Object getArg3() {
		return arg3;
	}

	public void setArg3(Object arg3) {
		this.arg3 = arg3;
	}

	public Object getArg4() {
		return arg4;
	}

	public void setArg4(Object arg4) {
		this.arg4 = arg4;
	}
}
