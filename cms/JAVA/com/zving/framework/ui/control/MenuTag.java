package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.html.HtmlElement;
import com.zving.framework.ui.util.TagUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 菜单标签　
 * 
 * @Author 王朝辉
 * @Date 2012-1-5
 * @Mail wang2@zving.com
 */
public class MenuTag extends AbstractTag {
	private String id;

	private String onitemclick;

	private String type;

	public static final Pattern PItem = Pattern.compile("<(li|a)(.*?)onclick=(\\\"|\\\')(.*?)\\2.*?>(.*?)</(li|a)>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "menu";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doAfterBody() throws TemplateRuntimeException {
		try {
			pageContext.getOut().write(getHtml(getBody()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EVAL_PAGE;
	}

	public String getHtml(String content) {
		String items = parseItems(content);
		StringBuilder sb = new StringBuilder();
		if (ObjectUtil.empty(id)) {
			id = TagUtil.getTagID(pageContext, "Menu");
		}
		sb.append("<div id=\"" + id + "\" class=\"z-menu z-hidden");
		if ("flat".equals(type)) {
			sb.append(" z-menu-flat");
		}
		sb.append("\">");
		sb.append(items);
		sb.append("</div>");
		sb.append("<script>");
		sb.append("new Zving.DropMenu('" + id + "');");
		sb.append("</script>");
		return sb.toString();
	}

	private String parseItems(String content) {
		StringBuilder sb = new StringBuilder();
		Matcher m = PItem.matcher(content);
		int lastIndex = 0;
		int i = 0;
		String innerText;
		String attr_onClick;
		while (m.find(lastIndex)) {
			String tmp = content.substring(lastIndex, m.start());
			if (StringUtil.isNotEmpty(tmp.trim())) {
				// sb.append(tmp);
			}
			String attrs = m.group(1);
			String id = HtmlElement.parseAttr(attrs).getString("id");
			innerText = m.group(5);
			innerText = LangUtil.get(innerText);
			attr_onClick = m.group(4);
			sb.append("<a id=\"" + id + "\" href=\"javascript:void(0);\" class=\"z-menu-item\" onclick=\"" + attr_onClick
					+ ";return false;\"" + " hidefocus >" + innerText + "</a>");
			lastIndex = m.end();
			i++;
		}
		if (lastIndex != content.length() - 1) {
			// sb.append(content.substring(lastIndex));
		}
		if (i != 0) {
			content = sb.toString();
		}
		return content;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOnitemclick() {
		return onitemclick;
	}

	public void setOnitemclick(String onitemclick) {
		this.onitemclick = onitemclick;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("type"));
		list.add(new TagAttr("onItemClick"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.MenuTagName}";
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
