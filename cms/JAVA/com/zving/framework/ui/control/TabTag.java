package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.ObjectUtil;

/**
 * 选项卡标签　
 * 
 * @Author 王育春
 * @Date 2008-4-18
 * @Mail wyuch@zving.com
 */
public class TabTag extends AbstractTag {
	private boolean lazy = true;
	private boolean cachedom = false;

	public static final String TabTagKey = "_ZVING_TABTAGKEY";

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "tab";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		pageContext.setAttribute(TabTagKey, new ArrayList<ChildTabTag>());
		context.getOut()
				.write("<table width=\"100%\" height=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" ztype=\"tab_table\" class=\"js_layoutTable\">");
		context.getOut().write("<tr><td height=\"37\" ztype=\"tab_thead\" valign=\"top\" style=\"_position:relative\">");
		context.getOut()
				.write("<div class=\"z-tabpanel\"><div class=\"z-tabpanel-ct\"><div class=\"z-tabpanel-overflow\"><div class=\"z-tabpanel-nowrap\"");
		if (lazy == false || cachedom == true) {
			context.getOut().write(" data-cache-dom=\"true\"");
		}
		context.getOut().write(">");
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doAfterBody() throws TemplateRuntimeException {
		try {
			context.getOut().write("</div></div></div></div>");
			context.getOut().write("</td></tr>");
			String selectedID = "";

			@SuppressWarnings("unchecked")
			ArrayList<ChildTabTag> children = (ArrayList<ChildTabTag>) pageContext.getAttribute(TabTagKey);
			if (ObjectUtil.notEmpty(children)) {
				for (ChildTabTag child : children) {
					if (child.isSelected()) {
						selectedID = child.getChildTabId();
						break;
					}
				}
				if (ObjectUtil.empty(selectedID)) {
					selectedID = children.get(0).getChildTabId();
				}

			}
			if (ObjectUtil.notEmpty(children)) {
				context.getOut().write("<tr><td height=\"*\" ztype=\"tab_tbody\" valign=\"top\">");
				context.getOut().write("<div");
				if (lazy == false || cachedom == true) {
					context.getOut().write(" data-cache-dom=\"true\"");
				}
				context.getOut().write("><div data-role=\"page\" data-external-page=\"false\">&nbsp;</div></div>");
				context.getOut().write("<script>");
				context.getOut().write("Zving.Page.onReady(function(){Zving.TabPage.init(");
				if (lazy) {
					context.getOut().write("true, ");
				} else {
					context.getOut().write("false, ");
				}
				context.getOut().write("\"" + selectedID + "\");},110);");
				context.getOut().write("</script>");
				context.getOut().write("</td></tr>");
			}
			context.getOut().write("</table>");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EVAL_PAGE;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public boolean isCachedom() {
		return cachedom;
	}

	public void setCachedom(boolean cachedom) {
		this.cachedom = cachedom;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("lazy", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("cachedom", TagAttr.BOOL_OPTIONS));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.TabTagName}";
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
