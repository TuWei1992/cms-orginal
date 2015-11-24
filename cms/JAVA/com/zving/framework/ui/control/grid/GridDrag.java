package com.zving.framework.ui.control.grid;

import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlTD;

/**
 * 拖拽
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-27
 */
public class GridDrag extends AbstractGridFeature {

	@Override
	public void rewriteTD(DataGridAction dga, HtmlTD th, HtmlTD td) {
		if (!"true".equalsIgnoreCase(th.getAttribute("drag"))) {
			return;
		}
		String style = td.getAttribute("style");
		if (style != null) {
			td.addAttribute("style", style);
		}
		td.addClassName("z-draggable");
	}
}
