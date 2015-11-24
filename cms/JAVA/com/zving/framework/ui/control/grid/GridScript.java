package com.zving.framework.ui.control.grid;

import com.zving.framework.Constant;
import com.zving.framework.collection.Filter;
import com.zving.framework.data.DataCollection;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataTable;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlElement;
import com.zving.framework.utility.FastStringBuilder;
import com.zving.framework.utility.Primitives;
import com.zving.framework.utility.StringUtil;

/**
 * 输出前端需要的样式
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-27
 */
public class GridScript extends AbstractGridFeature {
	public static final String Var = "DataGrid_Script";
	private static final String PlaceHolder = "${(" + Var + ")}";

	@Override
	public void rewriteBody(DataGridAction dga, DataGridBody body) {
		HtmlElement script = new HtmlElement("script");
		script.addAttribute("ztype", "DataGrid");
		script.setInnerHTML(PlaceHolder);
		body.getTemplateTable().addChild(script);
	}

	@Override
	public void appendScript(DataGridAction dga, FastStringBuilder scriptSB) {
		String id = dga.getID();
		if (!dga.isAjaxRequest()) {
			scriptSB.append("function DataGrid_").append(id).append("_Init(afterInit){");
			scriptSB.append("var dg=new Zving.DataGrid(document.getElementById('").append(id).append("'));");
			scriptSB.append("dg.setParam('").append(Constant.ID).append("','").append(id).append("');");
			for (String key : dga.getParams().keySet()) {
				Object v = dga.getParams().get(key);
				if (!key.equals(Constant.TagBody) && v != null) {
					if (v instanceof DataTable || v instanceof DAOSet) {// 处理后台传递到前台的DataTable
						DataTable dt = null;
						if (v instanceof DAOSet) {
							dt = ((DAOSet<?>) v).toDataTable();
						} else {
							dt = (DataTable) v;
						}
						scriptSB.append(DataCollection.dataTableToJS(dt));
						scriptSB.append("var _TmpDt = new DataTable();");
						scriptSB.append("_TmpDt.init(_Zving_Cols,_Zving_Values);");
						scriptSB.append("dg.setParam('").append(key).append("',_TmpDt);");
					} else if (Primitives.isPrimitives(v) || v instanceof String) {
						scriptSB.append("dg.setParam('").append(key).append("',\"").append(StringUtil.javaEncode(v.toString()))
								.append("\");");
					} else if (Primitives.isPrimitiveArray(v)) {
						scriptSB.append("dg.setParam('").append(key).append("',[").append(StringUtil.join(v)).append("]);");
					}// 其他的对象过滤掉
				}
			}
			scriptSB.append("dg.setParam('").append(Constant.DataGridMultiSelect).append("','").append(dga.isMultiSelect()).append("');");
			scriptSB.append("dg.setParam('").append(Constant.DataGridAutoFill).append("','").append(dga.isAutoFill()).append("');");
			scriptSB.append("dg.setParam('").append(Constant.DataGridScroll).append("','").append(dga.isScroll()).append("');");
			scriptSB.append("dg.setParam('").append(Constant.Lazy).append("','").append(dga.isLazy()).append("');");
			if (dga.getCacheSize() > 0) {
				scriptSB.append("dg.setParam('").append(Constant.CacheSize).append("','").append(dga.getCacheSize()).append("');");
			}
			scriptSB.append("dg.setParam('").append(Constant.Method).append("','").append(dga.getMethod()).append("');");
			scriptSB.append("dg.setParam('").append(Constant.Page).append("','").append(dga.isPageEnabled()).append("');");
			scriptSB.append("dg.setParam('").append(Constant.TagBody).append("', '").append(dga.getTagBody().getUID()).append("');");
			scriptSB.append("dg.setParam('").append(Constant.TemplateTR).append("', \"");
			DataCollection.encodeJSString(dga.getTagBody().getTemplateTR().getOuterHTML(), scriptSB);
			scriptSB.append("\");");
			scriptSB.append("if(afterInit){afterInit();}");
			scriptSB.append("}");
		}

		scriptSB.append("function DataGrid_").append(id).append("_Update(){");
		scriptSB.append("var dg=$('#").append(id).append("').getComponent('DataGrid');");
		scriptSB.append("if(!dg){return;}");
		DataTable dt = dga.getDataSource();
		if (dt != null) {
			if (dga.isPageEnabled()) {
				scriptSB.append("dg.setParam('").append(Constant.DataGridPageIndex).append("',").append(dga.getPageIndex()).append(");");
				scriptSB.append("dg.setParam('").append(Constant.DataGridPageTotal).append("'," + dga.getTotal()).append(");");
				scriptSB.append("dg.setParam('").append(Constant.Size).append("',").append(dga.getPageSize()).append(");");
			}
			// 所有下划线开始的列都不输出到JS中
			DataCollection.dataTableToJS(dt, new Filter<DataColumn>() {
				@Override
				public boolean filter(DataColumn dc) {
					return !dc.getColumnName().startsWith("_") || dc.getColumnName().equalsIgnoreCase("_RowNo");
				}
			}, scriptSB);
			if (dga.isLazy()) {
				scriptSB.append("var dt=dg.appendNodesData=new Zving.DataTable();");
			} else {
				scriptSB.append("var dt=dg.DataSource=new Zving.DataTable();");
			}
			scriptSB.append("dt.init(_Zving_Cols,_Zving_Values);");
		}
		if (!dga.isLazy()) {
			scriptSB.append("dg.restore();");
		}
		scriptSB.append("}");
		scriptSB.append("DataGrid_").append(id).append("_Update();");
		if (!dga.isAjaxRequest()) {
			scriptSB.append("Zving.Page.onLoad(function(){DataGrid_").append(id).append("_Init(DataGrid_").append(id)
					.append("_Update);},9);");
		}
	}
}
