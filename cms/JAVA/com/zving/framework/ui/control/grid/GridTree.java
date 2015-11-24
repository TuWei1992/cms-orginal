package com.zving.framework.ui.control.grid;

import com.zving.framework.Config;
import com.zving.framework.collection.Treex;
import com.zving.framework.collection.Treex.TreeIterator;
import com.zving.framework.collection.Treex.TreeNode;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlTD;
import com.zving.framework.utility.FastStringBuilder;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 树状列
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-27
 */
public class GridTree extends AbstractGridFeature {
	public static final String ZTYPE = "Tree";

	public static final String TreeLevelField = "_TreeLevel";

	public static final String TreeNodeHasChild = "_hasChild";

	private static final String KEY = "_GridTreeParam";

	public static class GridTreeParam {
		public String IdentifierColumnName = "ID";
		public String ParentIdentifierColumnName = "ParentID";
		public int StartLevel;
		public int ParentLevel;
		public String ParentID;
		public boolean isLazyLoad;
	}

	private GridTreeParam getParam(DataGridAction dga) {
		GridTreeParam gtp = (GridTreeParam) dga.getParams().get(KEY);
		if (gtp != null) {
			return gtp;
		}
		gtp = new GridTreeParam();
		HtmlTD th = null;
		for (HtmlTD td : dga.getTagBody().getHeadTR().getTDList()) {
			if (ZTYPE.equalsIgnoreCase(td.attributeValue("ztype"))) {
				th = td;
				break;
			}
		}
		if (th == null) {
			dga.getParams().put("_GridTreeParam", null);
			return null;
		}
		gtp.IdentifierColumnName = "ID";
		gtp.ParentIdentifierColumnName = "ParentID";
		int treeStartLevel = 999;
		if (th.hasAttribute("startLevel")) {
			treeStartLevel = Integer.parseInt(th.getAttribute("startLevel"));
			if (treeStartLevel <= 0) {
				treeStartLevel = 999;
			}
		}
		gtp.StartLevel = treeStartLevel;
		if (th.hasAttribute("idcolumn")) {
			String idcolumn = th.getAttribute("idcolumn");
			if (StringUtil.isNotEmpty(idcolumn)) {
				gtp.IdentifierColumnName = idcolumn;
			}
		}
		if (th.hasAttribute("parentidcolumn")) {
			String parentidcolumn = th.getAttribute("parentidcolumn");
			if (StringUtil.isNotEmpty(parentidcolumn)) {
				gtp.ParentIdentifierColumnName = parentidcolumn;
			}
		}
		// 如果是延迟请求，tree格式数据，则标记TreeLazyLoad=true
		if (StringUtil.isNotNull(dga.getParam("ParentLevel"))) {
			gtp.ParentLevel = Integer.parseInt(dga.getParam("ParentLevel"));
			gtp.ParentID = dga.getParam("ParentID");
			gtp.isLazyLoad = true;
		}
		dga.getParams().put("_GridTreeParam", gtp);
		return gtp;
	}

	@Override
	public void rewriteTD(DataGridAction dga, HtmlTD th, HtmlTD td) {
		if (!ZTYPE.equalsIgnoreCase(th.attributeValue("ztype"))) {
			return;
		}
		String field = th.getAttribute("field");
		String checked = th.getAttribute("checked");
		if (ObjectUtil.empty(checked) || checked.trim().length() == 0) {
			checked = "";
		} else {
			checked = checked.substring(0, checked.length() - 1) + "?\"checked='true'\":\"\"}";
		}
		StringBuilder cellSB = new StringBuilder();
		cellSB.append("<q style='padding:0 ${(" + TreeLevelField + "-1)*10}px'></q>${(_TreeIcon)}");
		if (field != null) {
			// 树的节点的值
			cellSB.append("<input type='checkbox'  name='").append(dga.getID()).append("_TreeRowCheck' id='").append(dga.getID())
					.append("_TreeRowCheck_${i}").append("' value='${" + field + "}' ").append(checked).append(" level='${")
					.append(TreeLevelField).append("}' onClick='treeCheckBoxClick(this);'>");
		}
		cellSB.append(td.getInnerHTML());
		td.setInnerHTML(cellSB.toString(), false);
		GridTreeParam gtp = getParam(dga);
		dga.getTagBody().getTemplateTR().addAttribute("treenodeid", "${" + gtp.IdentifierColumnName + "}");
		dga.getTagBody().getTemplateTR().addAttribute("level", "${" + TreeLevelField + "}");

		// lazy时不需要头部
		dga.getTagBody().getTemplateTable();
	}

	@Override
	public void beforeDataBind(DataGridAction dga, AbstractExecuteContext context, DataTable dataSource) {
		GridTreeParam gtp = getParam(dga);
		if (dataSource == null || gtp == null) {
			return;
		}
		// 如果要展现树形列，则会自动按ID,Parent排序并计算_TreeLevel
		if (dataSource.getDataColumn(gtp.ParentIdentifierColumnName) != null && dataSource.getDataColumn(gtp.IdentifierColumnName) != null) {
			DataTable dt = sortTreeDataTable(dataSource, gtp);
			for (int i = dataSource.getRowCount() - 1; i >= 0; i--) {
				dataSource.deleteRow(i);
			}
			dataSource.union(dt);
		}
		if (!dataSource.containsColumn("_TreeIcon")) {
			dataSource.insertColumn("_TreeIcon");
		}
		for (DataRow dr : dataSource) {
			int level = dr.getInt(TreeLevelField);
			boolean hasChild = "true".equals(dr.getString(TreeNodeHasChild));
			String icon = null;
			if (hasChild) {
				if (gtp.isLazyLoad || !gtp.isLazyLoad && gtp.StartLevel == level) {
					icon = "<img src='" + Config.getContextPath()
							+ "framework/images/butCollapse.gif' onclick='DataGrid.treeClick(this)'/>&nbsp;";
				} else {
					icon = "<img src='" + Config.getContextPath()
							+ "framework/images/butExpand.gif' onclick='DataGrid.treeClick(this)'/>&nbsp;";
				}
			} else {
				icon = "<img src='" + Config.getContextPath() + "framework/images/butNoChild.gif'/>&nbsp;";
			}
			dr.set("_TreeIcon", icon);
		}
	}

	@Override
	public void appendScript(DataGridAction dga, FastStringBuilder scriptSB) {
		GridTreeParam gtp = getParam(dga);
		String id = dga.getID();
		if (gtp == null || gtp.ParentID == null) {
			return;
		}
		scriptSB.append("var dg = $('#").append(id).append("').getComponent('DataGrid');");
		scriptSB.append("if (dg) {");
		scriptSB.append("dg.activateChildNodes('" + gtp.ParentID + "');");
		scriptSB.append("}");

	}

	/**
	 * 将DataTable按树形结构中的上下级关系排序
	 */
	public static DataTable sortTreeDataTable(DataTable dt, GridTreeParam gtp) {
		if (dt == null || dt.getRowCount() == 0) {
			return dt;
		}
		if (dt.getDataColumn(gtp.IdentifierColumnName) == null) {
			LogUtil.warn("DataGridAction.sortTreeDataTable():ID column not found:" + gtp.IdentifierColumnName);
		}
		if (dt.getDataColumn(gtp.ParentIdentifierColumnName) == null) {
			LogUtil.warn("DataGridAction.sortTreeDataTable():Parent column not found:" + gtp.ParentIdentifierColumnName);
		}
		if (dt.getDataColumn(TreeLevelField) != null) {
			dt.deleteColumn(TreeLevelField);
		}
		dt.insertColumn(new DataColumn(TreeLevelField, DataTypes.INTEGER));
		if (dt.getDataColumn(TreeNodeHasChild) != null) {
			dt.deleteColumn(TreeNodeHasChild);
		}
		dt.insertColumn(new DataColumn(TreeNodeHasChild, DataTypes.STRING));
		if (!dt.containsColumn("_RowNo")) {
			dt.insertColumn("_RowNo");
		}
		Treex<DataRow> tree = Treex.dataTableToTree(dt, gtp.IdentifierColumnName, gtp.ParentIdentifierColumnName);
		TreeIterator<DataRow> ti = tree.iterator();
		DataTable dest = new DataTable(dt.getDataColumns(), null);
		while (ti.hasNext()) {
			TreeNode<DataRow> node = ti.next();
			DataRow dr = node.getData();
			if (dr != null) {
				dr.set(TreeNodeHasChild, node.hasChild());

				if (gtp.isLazyLoad && gtp.ParentID != null) {
					// 如果是延迟载入的子节点
					if (dr.getString(gtp.ParentIdentifierColumnName).equalsIgnoreCase(gtp.ParentID)
							&& !dr.getString(gtp.IdentifierColumnName).equalsIgnoreCase(gtp.ParentID)) {
						dr.set(TreeLevelField, gtp.ParentLevel + 1);
						dest.insertRow(dr);
					}
				} else if (node.getLevel() <= gtp.StartLevel) {
					// 如果只载入特定层级
					dr.set(TreeLevelField, node.getLevel());
					dest.insertRow(dr);
				}
			}
		}
		for (int j = 0; j < dest.getRowCount(); j++) {
			dest.set(j, "_RowNo", new Integer(j + 1));// 这是因为GridTree在GridRow之后，GridRow已经设定好的RowNo会被打乱
		}
		return dest;
	}
}
