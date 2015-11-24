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
import com.zving.framework.ui.util.TagUtil;
import com.zving.framework.utility.ObjectUtil;

/**
 * 可折叠面板标签
 * 
 * @Author 王朝辉
 * @Date 2011-11-14
 * @Mail wang2@zving.com
 */
public class PanelHeaderTag extends AbstractTag {
	private String id;

	private String onClick;

	private boolean collapsible;

	private boolean collapsed;

	public static final Pattern PImg = Pattern.compile("^<img .*?src\\=.*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "panelheader";
	}

	@Override
	public void init() {
		collapsible = true;
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doAfterBody() throws TemplateRuntimeException {
		String content = getBody().trim();
		try {
			Matcher matcher = PImg.matcher(content);
			String img = null;
			String text = null;
			if (ObjectUtil.empty(id)) {
				id = TagUtil.getTagID(pageContext, "PanelHeader");
			}
			if (matcher.find()) {
				img = content.substring(matcher.start(), matcher.end());
				text = content.substring(matcher.end());
				pageContext.getOut().write(getHtml(id, img, text, collapsible, collapsed));
			} else {
				text = content;
				pageContext.getOut().write(getHtml(id, text, collapsible, collapsed));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EVAL_PAGE;
	}

	public static String getHtml(String id, String img, String text, boolean collapsible, boolean collapsed) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"z-panel-header\" id=\"").append(id).append("\">").append("<div class=\"z-panel-header-ct\">");
		if (collapsible != false) {
			sb.append("<a class=\"z-tool-toggle\" href=\"#;\">&nbsp;</a>");
		}
		text = LangUtil.get(text);
		sb.append("<b class=\"z-panel-header-text\">").append(text).append("</b>");
		sb.append("</div>").append("</div>");
		sb.append("<script>$(function(){new PanelHeader({el:'").append(id).append("',collapsed:").append(collapsed)
				.append("});});</script>");
		return sb.toString();
	}

	public static String getHtml(String id, String html, boolean collapsible, boolean collapsed) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"z-panel-header\" id=\"").append(id).append("\">").append("<div class=\"z-panel-header-ct\">");
		if (collapsible != false) {
			sb.append("<a class=\"z-tool-toggle\" href=\"#;\">&nbsp;</a>");
		}
		html = LangUtil.get(html);
		sb.append("<b class=\"z-panel-header-html\">").append(html).append("</b>");
		sb.append("</div>").append("</div>");
		sb.append("<script>$(function(){new PanelHeader({el:'").append(id).append("',collapsed:").append(collapsed)
				.append("});});</script>");
		return sb.toString();
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOnClick() {
		return onClick;
	}

	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}

	public boolean isCollapsible() {
		return collapsible;
	}

	public void setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("collapsed", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("collapsible", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("onClick"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.PanelHeaderTagName}";
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
