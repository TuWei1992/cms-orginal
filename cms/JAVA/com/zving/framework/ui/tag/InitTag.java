package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.ObjectUtil;

/**
 * 变量初始化标签，用于为<z:init>包围的区域中的表达式提供变量
 * 
 * @Author 王育春
 * @Date 2007-6-23
 * @Mail wyuch@zving.com
 */
public class InitTag extends AbstractTag {
	private String method;

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "init";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		if (ObjectUtil.notEmpty(method)) {
			IMethodLocator m = MethodLocatorUtil.find(method);
			PrivCheck.check(m);
			m.execute();
		}
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("method", true));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.Tag.InitTagName}";
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
