package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * 模板包含标签，在zhtml页面中包含文件，file相对于应用根目录
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-2-5
 */
public class IncludeTag extends AbstractTag {
	String file;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "include";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		AbstractExecuteContext includeContext = pageContext.getIncludeContext();
		includeContext.getManagerContext().getTemplateManager().execute(file, includeContext);
		return SKIP_BODY;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("file", DataTypes.STRING, "@{Framework.IncludeTag.File}"));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getDescription() {
		return "@{Framework.IncludeTag.Desc}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.IncludeTag.Name}";
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

}
