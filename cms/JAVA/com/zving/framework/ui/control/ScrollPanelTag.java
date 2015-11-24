package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.util.TagUtil;
import com.zving.framework.utility.ObjectUtil;

/**
 * 可滚动面板标签
 * 
 * @Author wang
 * @Date 2012-2-13
 * @Mail wyuch@zving.com
 */
public class ScrollPanelTag extends AbstractTag {
	private String id;

	private String targetId;

	private String theme;

	private String overflow;// 支持三种值为auto|y|x

	private boolean adaptive;

	public static final Pattern PId = Pattern.compile("^\\s*<div[^<>]+id\\=[\'\"]([\\w-]+)[\'\"]", Pattern.CASE_INSENSITIVE
			| Pattern.MULTILINE);

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "scrollpanel";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doAfterBody() throws TemplateRuntimeException {
		String content = getBody();
		Matcher matcher = PId.matcher(content);
		targetId = null;
		if (matcher.find() && matcher.group(1) != null) {
			targetId = matcher.group(1);
			if (ObjectUtil.empty(id)) {
				id = targetId + "_scrollpanel";
			}
		} else {
			if (ObjectUtil.empty(id)) {
				targetId = TagUtil.getTagID(pageContext, "ScrollPanel");
				id = targetId + "_scrollpanel";
			}
			content = content.replaceFirst("^\\s*<div\\b", "<div id=\"" + targetId + "\" ");
		}

		pageContext.getOut().write(content);

		pageContext.getOut().write("<script>");
		pageContext.getOut().write("Zving.Page.onLoad(function(){");
		pageContext.getOut().write("if(Zving.ScrollPanel)");
		pageContext.getOut().write(" new Zving.ScrollPanel({");
		if (ObjectUtil.notEmpty(overflow)) {
			pageContext.getOut().write("  overflow:'" + overflow + "',");
		}
		pageContext.getOut().write("  target:'" + targetId + "'");
		pageContext.getOut().write(" });");
		pageContext.getOut().write("});");
		pageContext.getOut().write("</script>");
		return EVAL_PAGE;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getOverflow() {
		return overflow;
	}

	public void setOverflow(String overflow) {
		this.overflow = overflow;
	}

	public boolean isAdaptive() {
		return adaptive;
	}

	public void setAdaptive(boolean adaptive) {
		this.adaptive = adaptive;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("id", true));
		list.add(new TagAttr("adaptive", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("overflow"));
		list.add(new TagAttr("targetID"));
		list.add(new TagAttr("theme"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.ScrollPanelTagName}";
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
