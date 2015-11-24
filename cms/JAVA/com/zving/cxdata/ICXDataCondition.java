package com.zving.cxdata;

import com.zving.framework.data.DataTable;
import com.zving.framework.extend.IExtendItem;
import com.zving.framework.utility.HtmlUtil;
/**
 * 数据获取条件服务扩展项基础类
 * @author v_zhouquan
 *
 */
public abstract class ICXDataCondition implements IExtendItem {
	public abstract String getArgName();
	public abstract DataTable getOptions();
	public String getFormHtml() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("<select id=\"" + getArgName() + "\" onChange=\"search();\">");
		sb.append(HtmlUtil.mapxToOptions(getOptions().toMapx(0, 1), "option", "", true));
		sb.append("</select>");
		return sb.toString();
	}
}
