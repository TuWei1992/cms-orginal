package com.zving.framework.ui.control.grid;

import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlTD;

/**
 * 选择器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-27
 */
public class GridSelector extends AbstractGridFeature {
	public static final String ZTYPE = "Selector";

	@Override
	public void rewriteTD(DataGridAction dga, HtmlTD th, HtmlTD td) {
		if (!ZTYPE.equalsIgnoreCase(th.getAttribute("ztype"))) {
			return;
		}
		th.addAttribute("disabledresize", "true");
		String field = th.getAttribute("field");
		String onSelect = th.getAttribute("onselect");
		if (onSelect == null) {
			onSelect = "";
		}
		String id = dga.getID();
		if (dga.isMultiSelect()) {
			th.setInnerHTML("<input type='checkbox' value='*' id='" + id + "_AllCheck' onclick=\"Zving.DataGrid.onAllCheckClick('" + id
					+ "', this)\" autoComplete='off' />");
		}
		String type = dga.isMultiSelect() ? "checkbox" : "radio";
		td.setInnerHTML("<input type='" + type + "' name='" + id + "_RowCheck' id='" + id + "_RowCheck_${i}' value='${" + field
				+ "}' onclick=\"return false\" autoComplete=\"off\" />");
		td.addAttribute("class", "dg-cell-selector");
		td.addAttribute("onclick", onSelect);
		td.addAttribute("ondblclick", "stopEvent(event);" + onSelect);
	}
}
