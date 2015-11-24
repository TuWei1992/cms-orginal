package com.zving.framework.extend.action;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;

/**
 * Zhtml扩展点定义标签　
 * 
 * @date 2009-11-7 <br>
 * @author 王育春 <br>
 * @email wangyc@zving.com <br>
 */
public class ExtendTag extends AbstractTag {

	private String id;

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int doStartTag() {
		ExtendManager.invoke(id, new Object[] { pageContext });
		return SKIP_BODY;
	}

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "extend";
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		return list;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ExtendTag.Name}";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}
}
