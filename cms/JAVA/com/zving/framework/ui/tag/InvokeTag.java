package com.zving.framework.ui.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.ITemplateCommand;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.command.TagCommand;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.StringUtil;

/**
 * 子过程调用标签，用于调用<z:sub>定义的子过程
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-2-5
 */
public class InvokeTag extends AbstractTag {
	String sub;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "invoke";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		@SuppressWarnings("unchecked")
		Mapx<String, TagCommand> subtagMap = (Mapx<String, TagCommand>) context.getExecutor().getAttributes().get(SubTag.KEY);

		int i = sub.indexOf('?');
		if (i > 0) {// 如果有参数，则置为局部变量
			String params = sub.substring(i + 1);
			sub = sub.substring(0, i);
			Mapx<String, String> map = StringUtil.splitToMapx(params, "&", "=");
			for (Entry<String, String> e : map.entrySet()) {
				context.addDataVariable(e.getKey(), e.getValue());
			}
		}

		if (subtagMap == null || !subtagMap.containsKey(sub)) {
			throw new TemplateRuntimeException("Template sub invoke failed,sub name " + sub + " not found!");
		}
		TagCommand subtagCommand = subtagMap.get(sub);
		for (ITemplateCommand command : subtagCommand.getCommands()) {
			if (command.execute(context) == AbstractTag.SKIP_PAGE) {
				break;
			}
		}
		return SKIP_BODY;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("sub", true, DataTypes.STRING, "@{Framework.ZInvokeTag.Sub}"));
		return list;
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

	@Override
	public String getDescription() {
		return "@{Framework.ZInvokeTag.Desc}";
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.ZInvokeTagName}";
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

}
