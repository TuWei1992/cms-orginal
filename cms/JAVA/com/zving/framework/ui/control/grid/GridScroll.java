package com.zving.framework.ui.control.grid;

import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlElement;
import com.zving.framework.ui.html.HtmlTD;
import com.zving.framework.ui.html.HtmlTR;
import com.zving.framework.ui.html.HtmlTable;
import com.zving.framework.utility.StringUtil;

/**
 * 选择器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-27
 */
public class GridScroll extends AbstractGridFeature {
	@Override
	public void rewriteBody(DataGridAction dga, DataGridBody body) {
		if (!dga.isScroll()) {
			return;
		}
		HtmlTable table = body.getTemplateTable();
		table.setTBody(false);
		table.setTHead(false);
		table.getTR(0).setClassName("dg-headTr");

		String id = dga.getID();
		String fw = table.getAttribute("fixedWidth");
		String fh = table.getAttribute("fixedHeight");

		// 加入固定头部
		HtmlElement outer = new HtmlElement("div");
		outer.setID(id + "_outer");
		outer.setClassName("z-datagrid-outer dg-scrollable dg-nobr");
		outer.setAttribute("ztype", "_DataGridWrapper");
		if (StringUtil.isNotEmpty(fw)) {
			outer.setStyle("width:" + fw);
		}

		HtmlElement dock = new HtmlElement("div");
		dock.parseHtml("<div id='" + id + "_dock' class='dg-dock'><div id='" + id + "_dock_trigger' "
				+ "class='dock-trigger'></div><div id='" + id + "_dock_station' class='dock-station'></div></div>");
		dock.setParent(outer);

		// 头部
		HtmlElement outerHead = new HtmlElement("div");
		outerHead.setID(id + "_outer_head");
		outerHead.setClassName("dg-head");
		outerHead.setParent(outer);

		HtmlTable headTable = table.clone();
		headTable.getChildren().clear();
		HtmlTR tmpTR = table.getTR(0).clone();
		headTable.addChild(tmpTR);
		headTable.setID(id + "_head");
		headTable.setClassName("dg-headTable");
		tmpTR.setClassName("dg-headTr");
		for (HtmlTD td : tmpTR.getTDList()) {
			td.setInnerHTML("<div class='dg-th'>" + td.getInnerHTML() + "</div>");
		}
		headTable.setParent(outerHead);

		// 加入DataGrid本身
		HtmlElement outerBody = new HtmlElement("div");
		outerBody.setID(id + "_outer_body");
		outerBody.setClassName("dg-body");
		outerBody.setParent(outer);

		String style = "";
		if (StringUtil.isNotEmpty(fw) && fw.indexOf("%") < 0) {
			style += "width:" + fw + ";";
		}
		if (StringUtil.isNotEmpty(fh)) {
			style += "height:" + fh + ";";
		}
		outerBody.setStyle(style);
		table.setTHead(true);
		table.setTBody(true);
		outerBody.addText("</z:if>");
		table.setParent(outerBody);
		outerBody.addText("<z:if condition='${!_DataGridAction.AjaxRequest}'>");

		// 加入分页条
		if (dga.isPageEnabled() && body.getPageBarTR() != null) {
			table.removeTR(table.getTRList().size() - 1);// 最后一行是pagebar

			HtmlElement outerFoot = new HtmlElement("div");
			outerFoot.setClassName("dg-foot");
			outerFoot.setParent(outer);

			HtmlTable footTable = new HtmlTable();
			footTable.addAttribute("width", "100%");
			footTable.addTR(body.getPageBarTR());
			footTable.setParent(outerFoot);
		}

		// 替换template
		body.getTemplate().getChildren().clear();
		body.getTemplate().addText("<z:if condition='${!_DataGridAction.AjaxRequest}'>");
		outer.setParent(body.getTemplate());
		body.getTemplate().addText("</z:if>");
	}

}
