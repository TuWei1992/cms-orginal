package com.zving.framework.ui.control.grid;

import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlTR;
import com.zving.framework.utility.ObjectUtil;

/**
 * 交替样式
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-2-27
 */
public class GridAlternatingStyle extends AbstractGridFeature {

	@Override
	public void rewriteTR(DataGridAction dga, HtmlTR tr) {
		if (!"Template".equalsIgnoreCase(tr.getAttribute("ztype"))) {
			return;
		}
		String style1 = tr.getAttribute("style1");
		String style2 = tr.getAttribute("style2");
		String class1 = tr.getAttribute("class1");
		String class2 = tr.getAttribute("class2");
		tr.removeAttribute("style1");
		tr.removeAttribute("style2");
		tr.removeAttribute("class1");
		tr.removeAttribute("class2");
		tr.addAttribute("class", "dg-row");
		if (ObjectUtil.notEmpty(style1)) {
			if (ObjectUtil.empty(style2)) {
				tr.addAttribute("style", style1);
			} else {
				tr.addAttribute("style", "${i%2==0?'" + style1 + "':'" + style2 + "'}");
			}
		} else if (ObjectUtil.notEmpty(style2)) {
			tr.addAttribute("style", style2);
		}
		if (ObjectUtil.notEmpty(class1)) {
			if (ObjectUtil.empty(class2)) {
				tr.addAttribute("class", "dg-row " + class1);
			} else {
				tr.addAttribute("class", "dg-row ${i%2==0?'" + class1 + "':'" + class2 + "'}");
			}
		} else if (ObjectUtil.notEmpty(class2)) {
			tr.addAttribute("class", "dg-row " + class2);
		}
	}
}
