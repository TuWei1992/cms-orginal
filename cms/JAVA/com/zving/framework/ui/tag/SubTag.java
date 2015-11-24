package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.template.command.TagCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;

/**
 * 子过程定义标签，定义好的模板过程可以使用z:invoke调用，并可以递归调用。<br>
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-2-5
 */
public class SubTag extends AbstractTag {
	public static final String KEY = "_ZVING_SUBTAG_MAP";

	private String name;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "sub";
	}

	@Override
	public void afterCompile(TagCommand command, TemplateExecutor executor) {
		@SuppressWarnings("unchecked")
		Mapx<String, TagCommand> subtagMap = (Mapx<String, TagCommand>) executor.getAttributes().get(KEY);
		if (subtagMap == null) {
			subtagMap = new Mapx<String, TagCommand>();
			executor.getAttributes().put(KEY, subtagMap);
		}
		subtagMap.put(name, command);
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		return SKIP_BODY;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("name", true, DataTypes.STRING, "@{Framework.ZSubTag.Name}"));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZSubTag.Desc}";
	}

	@Override
	public String getExtendItemName() {
		if (pageContext == null) {
			return "@{Framework.ZSubTagName}";
		} else {
			return name;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

}
