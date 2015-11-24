package com.zving.framework.i18n;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 国际化字段按钮，用于在zhtml中为一个文本框/文本域添加国际化数据填写按钮。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-11
 */
public class LangButtonTag extends AbstractTag {

	private String target;

	private String value;

	@Override
	public int doStartTag() {
		try {
			if (LangUtil.getSupportedLanguages().size() > 0) {
				StringBuilder sb = new StringBuilder();
				if (ObjectUtil.empty(value)) {
					value = "";
				}
				sb.append("<input type=\"hidden\" value=\"").append(StringUtil.escape(value)).append("\" id=\"" + target + "_I18N\">");
				sb.append("<img src=\"")
						.append(Config.getContextPath())
						.append("icons/extra/i18n.gif\" align=\"absmiddle\" style=\"cursor:pointer\" onclick=\"Zving.Lang.onLangButtonClick('")
						.append(target).append("')\"/>");
				sb.append("<script>Zving.Page.onReady(function(){Zving.Node.setValue(\"" + target + "\",\"" + LangUtil.decode(value)
						+ "\");});</script>");
				pageContext.getOut().write(sb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "langbutton";
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("target", true));
		list.add(new TagAttr("value"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.LangButtonTag.Name}";
	}

	@Override
	public String getDescription() {
		return "@{Framework.LangButtonTag.Desc}";
	}

	@Override
	public String getPluginID() {
		return FrameworkPlugin.ID;
	}

}
