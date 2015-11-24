package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.Constant;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;
import com.zving.framework.extend.plugin.FrameworkPlugin;
import com.zving.framework.i18n.LangMapping;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.ui.IPageEnableAction;
import com.zving.framework.ui.tag.ListAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.ServletUtil;
import com.zving.framework.utility.StringFormat;
import com.zving.framework.utility.StringUtil;

/**
 * 分页条标签　
 * 
 * @Author 王育春
 * @Date 2009-3-24
 * @Mail wyuch@zving.com
 */
public class PageBarTag extends AbstractTag {
	private String target;

	private int type;// 0完整模式,1简化,2最简

	private int total;

	private int pageIndex;

	private int pageSize;

	private boolean afloat;

	private boolean autoHide;// 没有记录则自动隐藏

	protected IPageEnableAction action;

	@Override
	public String getPrefix() {
		return "z";
	}

	@Override
	public String getTagName() {
		return "pagebar";
	}

	@Override
	public int doStartTag() throws TemplateRuntimeException {
		action = (IPageEnableAction) pageContext.getAttribute(target + Constant.ActionInPageContext);
		if (action == null) {
			return SKIP_BODY;
		}
		total = action.getTotal();
		pageIndex = action.getPageIndex();
		pageSize = action.getPageSize();
		pageContext.setAttribute("PageTotal", total);
		pageContext.setAttribute("PageIndex", pageIndex);
		pageContext.setAttribute("PageSize", pageSize);
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws TemplateRuntimeException {
		try {
			String html = "";
			if (total != 0 || !autoHide) {// 没数据自动隐藏
				html = getBody().trim();
				if (ObjectUtil.empty(html)) {
					html = getPageBarHtml(target, type, total, pageSize, pageIndex);
					if (action instanceof DataListAction || action instanceof DataGridAction) {
						if (action instanceof DataListAction) {
							html = html + "<script>Zving.DataList.setParam('" + target + "','PageBarType'," + type + ");";
						} else if (action instanceof DataGridAction) {
							html = html + "<script>Zving.DataGrid.setParam('" + target + "','PageBarType'," + type + ");";
						}
						if (afloat) {
							html = html + "new Zving.Afloat(Zving.getDom('_PageBar_" + target + "').parentNode);";
						}
						html = html + "</script>";
					}
				}
			}
			pageContext.getOut().write(html);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EVAL_PAGE;
	}

	public String getPageBarHtml(String target, int type, int total, int pageSize, int pageIndex) {
		return getDefault(target, type, total, pageSize, pageIndex);
	}

	private String getDefault(String target, int type, int total, int pageSize, int pageIndex) {
		String first = LangMapping.get("Framework.DataGrid.FirstPage");
		String prev = LangMapping.get("Framework.DataGrid.PreviousPage");
		String next = LangMapping.get("Framework.DataGrid.NextPage");
		String last = LangMapping.get("Framework.DataGrid.LastPage");
		String gotoPage = LangMapping.get("Framework.DataGrid.GotoPage");
		String gotoEnd = LangMapping.get("Framework.DataGrid.GotoPageEnd");
		String error = LangMapping.get("Framework.DataGrid.ErrorPage");
		String gotoButton = LangMapping.get("Framework.DataGrid.Goto");
		String pagebar = LangMapping.get("Framework.DataGrid.PageBar");
		String records = LangMapping.get("Framework.DataGrid.Records");

		String jsClass = "";
		if (action instanceof DataListAction) {
			jsClass = "DataList";
		} else if (action instanceof DataGridAction) {
			jsClass = "DataGrid";
		}
		StringBuilder sb = new StringBuilder();
		int totalPages = new Double(Math.ceil(total * 1.0 / pageSize)).intValue();
		sb.append("<div id='_PageBar_").append(target).append("' class=\"pagebar_wrap\" pagebartype=\"" + type + "\">");
		sb.append("<div style=\"float:right;\" class=\"tfoot-fr\"><div class=\"pagebar\">&nbsp;");
		String queryString = null;
		if (action instanceof ListAction) {
			queryString = ((ListAction) action).getQueryString();
			if (StringUtil.isEmpty(queryString)) {
				queryString = "?PageIndex=";
			} else {
				Mapx<String, String> map = StringUtil.splitToMapx(queryString, "&", "=");
				map.remove("PageIndex");
				if (map.size() == 0) {
					queryString = "?PageIndex=";
				} else {
					queryString = ServletUtil.getQueryStringFromMap(map) + "&PageIndex=";
				}
			}
		}
		if (pageIndex > 0) {
			if (action instanceof DataListAction || action instanceof DataGridAction) {
				sb.append("<span class=\"first\"><a href='javascript:void(0);' onclick=\"Zving." + jsClass + ".firstPage('").append(target)
						.append("');\">").append(first).append("</a></span>");
				sb.append("<span class=\"previous\"><a href='javascript:void(0);' onclick=\"Zving." + jsClass + ".previousPage('")
						.append(target).append("');\">").append(prev).append("</a></span>");
			} else {// ListTag下pageIndex在URL中是从1开始的
				sb.append("<a href='").append(queryString).append("1'>").append(first).append("</a>&nbsp;|&nbsp;");
				sb.append("<a href='").append(queryString).append(pageIndex).append("'>").append(prev).append("</a>&nbsp;|&nbsp;");
			}
		} else {
			sb.append("<span class=\"first\">").append(first).append("</span>");
			sb.append("<span class=\"previous\">").append(prev).append("</span>");
		}
		if (totalPages != 0 && pageIndex + 1 != totalPages) {
			if (action instanceof DataListAction || action instanceof DataGridAction) {
				sb.append("<span class=\"next\"><a href='javascript:void(0);' onclick=\"Zving." + jsClass + ".nextPage('").append(target)
						.append("');\">").append(next).append("</a></span>");
				sb.append("<span class=\"last\"><a href='javascript:void(0);' onclick=\"Zving." + jsClass + ".lastPage('").append(target)
						.append("');\">").append(last).append("</a></span>");
			} else {// ListTag下pageIndex在URL中是从1开始的
				sb.append("<a href='").append(queryString).append(pageIndex + 2).append("'>").append(next).append("</a>&nbsp;|&nbsp;");
				sb.append("<a href='").append(queryString).append(totalPages).append("'>").append(last).append("</a>&nbsp;|&nbsp;");
			}
		} else {
			sb.append("<span class=\"next\">").append(next).append("</span>");
			sb.append("<span class=\"last\">").append(last).append("</span>");
		}

		sb.append("&nbsp;").append(gotoPage).append("&nbsp;<input id='_PageBar_Index_").append(target)
				.append("' type='text' class='inputText gotopage' value=\"").append(pageIndex + 1).append("\"");
		sb.append(
				"onkeyup=\"value=value.replace(/\\D/g,'');style.width=Math.min(7*(value.length||1)+1,36)+'px';\" onkeydown=\"if(Zving.getEvent().keyCode==13)document.getElementById('_PageBar_JumpBtn_")
				.append(target).append("').onclick();\">");
		sb.append("/<span class=\"js_totalPages\">").append(totalPages).append("</span>");
		sb.append(gotoEnd).append("&nbsp;");
		sb.append("<input type='button' id='_PageBar_JumpBtn_").append(target)
				.append("' class='pageJumpBtn' onclick=\"var v=document.getElementById('_PageBar_Index_").append(target)
				.append("').value,t=Number(Zving.DataList.getParam('").append(target)
				.append("', Constant.PageTotal)),s=Number(Zving.DataList.getParam('").append(target)
				.append("', Constant.Size)),l=Math.floor(t/s)+(t%s===0?0:1);if(!/^\\d+$/.test(v)").append("||v<1||v>l){alert('")
				.append(error).append("');document.getElementById('_PageBar_Index_").append(target).append("').focus();}")
				.append("else{var pageIndex = v>0?v-1:0;");
		if (action instanceof DataListAction || action instanceof DataGridAction) {
			sb.append("Zving." + jsClass + ".setParam('").append(target).append("','").append(Constant.DataGridPageIndex)
					.append("',pageIndex);Zving." + jsClass + ".loadData('").append(target).append("');");
		} else {
			sb.append("if('" + queryString + "'!='null'){");
			sb.append("window.location='").append(queryString).append("'+(pageIndex+1);");
			sb.append("}");
		}
		sb.append("}\" value='").append(gotoButton).append("'>");

		sb.append("</div></div>");
		sb.append("<div style=\"float:left;\" class=\"tfoot-fl\">");
		sb.append("<span class=\"pageInfo_trigger\" style=\"display:none;\">");
		if (type == 2) {
			sb.append("&gl;&gl;");
		} else {
			sb.append(new StringFormat(records, "<span class=\"js_total\">" + total + "</span>"));
		}
		sb.append("</span>");
		sb.append("<span class=\"pageInfo\">");
		sb.append(new StringFormat(pagebar, "<span class=\"js_total\">" + total + "</span>", "<span class=\"inline-block pageSizeGroup\">"
				+ "<ul class=\"sizeList\" style=\"position:absolute;\">" + "<li onclick=\"Zving.DataList.changePageSize('" + target
				+ "',10)\">10</li>" + "<li onclick=\"Zving.DataList.changePageSize('" + target + "',15)\">15</li>"
				+ "<li onclick=\"Zving.DataList.changePageSize('" + target + "',20)\">20</li>"
				+ "<li onclick=\"Zving.DataList.changePageSize('" + target + "',30)\">30</li>"
				+ "<li onclick=\"Zving.DataList.changePageSize('" + target + "',50)\">50</li>" + "</ul>" + "<span class=\"js_pageSize\">"
				+ pageSize + "</span>" + "</span>", "<span class=\"js_pageIndex\">" + (totalPages == 0 ? 0 : pageIndex + 1) + "</span>",
				"<span class=\"js_totalPages\">" + totalPages + "</span>"));
		sb.append("</span>");
		sb.append("</div><div style=\"clear:both;height:0;overflow:hidden;\"></div></div>");
		return sb.toString();
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isAfloat() {
		return afloat;
	}

	public void setAfloat(boolean afloat) {
		this.afloat = afloat;
	}

	public boolean isAutoHide() {
		return autoHide;
	}

	public void setAutoHide(boolean autoHide) {
		this.autoHide = autoHide;
	}

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> list = new ArrayList<TagAttr>();
		list.add(new TagAttr("afloat", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("autoHide", TagAttr.BOOL_OPTIONS));
		list.add(new TagAttr("pageIndex", DataTypes.INTEGER));
		list.add(new TagAttr("pageSize", DataTypes.INTEGER));
		list.add(new TagAttr("total", DataTypes.INTEGER));
		list.add(new TagAttr("type", DataTypes.INTEGER));
		list.add(new TagAttr("target"));
		return list;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.UIControl.PageBarTagName}";
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
