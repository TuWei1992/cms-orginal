package com.zving.framework.ui.control.grid;

import java.util.List;
import java.util.regex.Pattern;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlTD;
import com.zving.framework.ui.html.HtmlTR;
import com.zving.framework.utility.FastStringBuilder;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 排序
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-27
 */
public class GridSort extends AbstractGridFeature {

	private static Pattern SortPattern = Pattern.compile("[\\w\\,\\s]*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	@Override
	public void rewriteTR(DataGridAction dga, HtmlTR tr) {
		if (!DataGridBody.TR_HEAD.equalsIgnoreCase(tr.getAttribute("ztype"))) {
			return;
		}
		List<HtmlTD> list = tr.getTDList();
		StringBuilder sortSB = new StringBuilder();
		boolean first = true;
		for (HtmlTD th : list) {
			String sortField = th.getAttribute("sortField");
			String direction = th.getAttribute("direction");
			if (StringUtil.isNotEmpty(sortField)) {
				if (StringUtil.isNotEmpty(direction)) {// 未指定方向的先不需要排序，等在页面中点击后再排序
					if (!first) {
						sortSB.append(",");
					}
					sortSB.append(sortField);
					sortSB.append(" ");
					sortSB.append(direction);
					first = false;
				}
			}
			if (StringUtil.isNotEmpty(sortField)) {
				th.addAttribute("class", "dg-sortTh");
				th.addAttribute("onClick", "Zving.DataGrid.onSort(this);");
				StringBuilder sb = new StringBuilder();
				sb.append("<span style='float:left'>");
				sb.append(th.getInnerHTML());
				sb.append("</span>");
				sb.append("<img src='");
				sb.append(Config.getContextPath());
				sb.append("framework/images/blank.gif'");
				sb.append(" class='fr icon_sort");
				if (StringUtil.isNotEmpty(direction)) {
					sb.append(direction.toUpperCase());
				}
				sb.append("' width='12' height='12'>");
				th.setInnerHTML(sb.toString());
			}
		}
		String sort = sortSB.toString();
		dga.getParams().put(Constant.DataGridSortString, sort);
		tr.getTable().addAttribute("sortstring", sort);
	}

	@Override
	public void appendScript(DataGridAction dga, FastStringBuilder scriptSB) {
		String sort = dga.getParam(Constant.DataGridSortString);
		String id = dga.getID();
		if (ObjectUtil.notEmpty(sort)) {
			scriptSB.append("var dg = $('#").append(id).append("').getComponent('DataGrid');");
			scriptSB.append("if (dg) {");
			scriptSB.append("dg.setParam('").append(Constant.DataGridSortString).append("','").append(sort).append("');");
			scriptSB.append("}");
		}
	}

	public static boolean isSortFlag(DataGridAction dga) {
		return ObjectUtil.notEmpty(dga.getParam(Constant.DataGridSortString));
	}

	public static String getSortString(DataGridAction dga) {
		String sort = dga.getParam(Constant.DataGridSortString);
		if (ObjectUtil.notEmpty(sort) && SortPattern.matcher(sort).matches()) {
			return " order by " + sort;
		}
		return "";
	}

}
