package com.zving.framework.ui.control.datalist;

import com.zving.framework.Constant;
import com.zving.framework.template.TemplateCompiler;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.ui.control.DataListAction;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;
import com.zving.framework.utility.FastStringBuilder;
import com.zving.framework.utility.StringUtil;

/**
 * DataList的标签体
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-2
 */
public class DataListBody {
	String uid;
	String source;
	String template;
	TemplateExecutor executor;

	public DataListBody(String uid, String source) {
		this.uid = uid;
		this.source = source;
		this.source = source.substring(source.indexOf('>') + 1, source.lastIndexOf("</z:datalist>"));
	}

	public String getUID() {
		return uid;
	}

	public void compile(DataListAction dla) {
		template = rewrite(dla);
		TemplateCompiler tc = new TemplateCompiler(ZhtmlManagerContext.getInstance());
		tc.compileSource(template);
		executor = tc.getExecutor();
	}

	String rewrite(DataListAction dla) {
		FastStringBuilder sb = new FastStringBuilder();
		String ID = dla.getID();
		sb.append("<!--_ZVING_DATALIST_START_").append(ID).append("-->\n");
		sb.append("<input type=\"hidden\" id=\"").append(ID).append("\" method=\"").append(dla.getMethod()).append("\"");
		if (dla.isPageEnabled()) {
			sb.append(" page=\"true\"");
		}
		if (dla.getPageSize() > 0) {
			sb.append(" size=\"").append(dla.getPageSize()).append("\"");
		}
		if (dla.isAutoFill()) {
			sb.append(" autofill=\"true\"");
		}
		if (dla.isAutoPageSize()) {
			sb.append(" autopagesize=\"true\"");
		}
		if (dla.getDragClass() != null) {
			sb.append(" dragclass=\"").append(dla.getDragClass()).append("\"");
		}
		if (dla.getListNodes() != null) {
			sb.append(" listnodes=\"").append(dla.getListNodes()).append("\"");
		}
		if (dla.getSortEnd() != null) {
			sb.append(" sortend=\"").append(dla.getSortEnd()).append("\"");
		}
		sb.append("/>");
		sb.append("<z:list>");
		sb.append(source);
		sb.append("</z:list>");
		sb.append("<script ztype='DataList'>");
		getScript(dla, sb);
		sb.append("</script>");
		sb.append("\n<!--_ZVING_DATALIST_END_").append(ID).append("-->");
		return sb.toStringAndClose();
	}

	void getScript(DataListAction dla, FastStringBuilder sb) {
		String ID = dla.getID();
		sb.append("<z:if condition='${!_DataListAction.AjaxRequest}'>");
		sb.append("function DataList_").append(ID).append("_Init(afterInit){");
		sb.append("var dl = new Zving.DataList(document.getElementById('").append(ID).append("'));");
		sb.append("dl.setParam('").append(Constant.ID).append("','").append(ID).append("');");
		sb.append("<z:foreach data='${_DataListAction.Params}'>");
		sb.append("dl.setParam('${Key}',\"${javaEncode(Value)}\");");
		sb.append("</z:foreach>");
		sb.append("dl.setParam('").append(Constant.Page).append("',").append(dla.isPageEnabled()).append(");");
		if (StringUtil.isNotEmpty(dla.getDragClass())) {
			sb.append("dl.setParam('").append(Constant.DragClass).append("','").append(dla.getDragClass()).append("');");
		}
		if (StringUtil.isNotEmpty(dla.getSortEnd())) {
			sb.append("dl.setParam('").append(Constant.SortEnd).append("','").append(dla.getSortEnd()).append("');");
		}
		sb.append("dl.setParam('" + Constant.TagBody + "', '").append(uid).append("');");
		sb.append("if(afterInit){afterInit();}");
		sb.append("}");
		sb.append("</z:if>");

		sb.append("function DataList_").append(ID).append("_Update(){");
		sb.append("var dl = $('#").append(ID).append("').getComponent('DataList');");
		sb.append("if(!dl){return;}");

		sb.append("dl.setParam('").append(Constant.DataGridPageIndex).append("',${_DataListAction.PageIndex});");
		sb.append("dl.setParam('").append(Constant.DataGridPageTotal).append("',${_DataListAction.Total});");
		sb.append("dl.setParam('").append(Constant.Size).append("',${_DataListAction.PageSize});");

		sb.append("}");
		sb.append("DataList_").append(ID).append("_Update();");
		sb.append("<z:if condition='${!_DataListAction.AjaxRequest}'>");
		sb.append("Zving.Page.onLoad(function(){DataList_").append(ID).append("_Init(DataList_").append(ID).append("_Update);}, 8);");
		sb.append("</z:if>");
	}

	public TemplateExecutor getExecutor() {
		return executor;
	}

	public String getTemplate() {
		return template;
	}
}
