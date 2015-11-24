package com.zving.framework.ui.control.grid;

import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlTD;

/**
 * 行号
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-27
 */
public class GridRowNo extends AbstractGridFeature {
	public static final String ZTYPE = "RowNo";

	@Override
	public void beforeDataBind(DataGridAction dga, AbstractExecuteContext context, DataTable dataSource) {
		if (dataSource != null) {
			if (dataSource.getDataColumn("_RowNo") == null) {
				dataSource.insertColumn(new DataColumn("_RowNo", DataTypes.INTEGER));
			}
			for (int j = 0; j < dataSource.getRowCount(); j++) {
				int rowNo = dga.getPageIndex() * dga.getPageSize() + j + 1;
				dataSource.set(j, "_RowNo", new Integer(rowNo));
			}
		}

	}

	@Override
	public void rewriteTD(DataGridAction dga, HtmlTD th, HtmlTD td) {
		boolean rowNoFlag = ZTYPE.equalsIgnoreCase(th.getAttribute("ztype"));
		if (!rowNoFlag) {
			return;
		}
		th.addAttribute("disabledresize", "true");
		td.addAttribute("rowno", "${_RowNo}");
		td.addClassName("rowNo");
		td.setInnerHTML("${_RowNo}");
	}
}
