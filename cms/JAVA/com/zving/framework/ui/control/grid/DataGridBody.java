package com.zving.framework.ui.control.grid;

import java.util.List;

import com.zving.framework.template.TemplateCompiler;
import com.zving.framework.template.TemplateExecutor;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.html.HtmlDocument;
import com.zving.framework.ui.html.HtmlElement;
import com.zving.framework.ui.html.HtmlNode;
import com.zving.framework.ui.html.HtmlParser;
import com.zving.framework.ui.html.HtmlTD;
import com.zving.framework.ui.html.HtmlTR;
import com.zving.framework.ui.html.HtmlTable;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;

/**
 * DataGrid标签体
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-2
 */
public class DataGridBody {
	public static final String TR_TEMPLATE = "template";
	public static final String TR_HEAD = "head";
	public static final String TR_PAGEBAR = "pagebar";
	public static final String TR_SIMPLEPAGEBAR = "simplepagebar";

	String uid;
	HtmlDocument source;
	HtmlDocument template;
	TemplateExecutor executor;
	HtmlTR templateTR;
	HtmlTR headTR;
	HtmlTR pageBarTR;
	HtmlTable templateTable;

	public DataGridBody(String uid, String tagbodyHtml) {
		this.uid = uid;
		HtmlParser p = new HtmlParser(tagbodyHtml);
		p.parse();
		source = new HtmlDocument();
		for (HtmlNode node : p.getDocument().element("z:datagrid").getChildren()) {
			source.addChild(node);
		}
		template = source.clone();
		templateTable = new HtmlTable(template.getTopElementsByTagName("table").get(0));
		templateTable.setTHead(true);
		templateTable.setTBody(true);
	}

	public String getUID() {
		return uid;
	}

	void rewrite(DataGridAction dga) {
		List<HtmlTR> trs = templateTable.getTRList();
		for (HtmlTR tr : trs) {
			if (TR_HEAD.equalsIgnoreCase(tr.getAttribute("ztype"))) {
				headTR = tr;
			} else if (TR_TEMPLATE.equalsIgnoreCase(tr.getAttribute("ztype"))) {
				templateTR = tr;
			} else if (TR_PAGEBAR.equalsIgnoreCase(tr.getAttribute("ztype"))) {
				pageBarTR = tr;
			} else if (TR_SIMPLEPAGEBAR.equalsIgnoreCase(tr.getAttribute("ztype"))) {
				pageBarTR = tr;
			} else {
				templateTR = tr;
				templateTR.setAttribute("ztype", TR_TEMPLATE);
			}
		}
		if (headTR == null) {
			headTR = trs.get(0);
			headTR.setAttribute("ztype", TR_HEAD);
		}
		headTR.addAttribute("onselectstart", "return false;");
		for (HtmlTD td : headTR.getTDList()) {
			td.setTH(true);
		}

		if (!dga.isPageEnabled() && pageBarTR != null) {
			int i = 0;
			for (HtmlTR tr : trs) {
				if (pageBarTR == tr) {
					templateTable.removeTR(i);
					break;
				}
				i++;
			}
			pageBarTR = null;
		}

		templateTable.addAttribute("id", dga.getID());
		templateTable.addAttribute("page", "" + dga.isPageEnabled());
		templateTable.addAttribute("size", "" + dga.getPageSize());
		templateTable.addAttribute("method", dga.getMethod());
		templateTable.addAttribute("multiselect", "" + dga.isMultiSelect());
		templateTable.addAttribute("autofill", "" + dga.isAutoFill());
		templateTable.addAttribute("autopagesize", "" + dga.isAutoPageSize());
		templateTable.addAttribute("scroll", "" + dga.isScroll());
		templateTable.addAttribute("lazy", "" + dga.isLazy());
		templateTable.addAttribute("cachesize", "" + dga.getCacheSize());

		dga.setTagBody(this);

		// 先重写td/th
		List<HtmlTD> ths = headTR.getTDList();
		List<HtmlTD> tds = templateTR.getTDList();
		for (int i = 0; i < ths.size(); i++) {
			if (tds.size() <= i) {
				break;
			}
			for (AbstractGridFeature f : FeatureManager.getInstance().getAll()) {
				f.rewriteTD(dga, ths.get(i), tds.get(i));
			}
		}

		// 再重写tr
		for (HtmlTR tr : trs) {
			for (AbstractGridFeature f : FeatureManager.getInstance().getAll()) {
				f.rewriteTR(dga, tr);
			}
		}

		// 最后重写整个body. 注意：不能先重写body，因为GridScroll的原因，会出现td/th/tr的重写不能反馈到模板中去的现象
		for (AbstractGridFeature f : FeatureManager.getInstance().getAll()) {
			f.rewriteBody(dga, this);
		}

	}

	public void compile(DataGridAction dga) {
		rewrite(dga);
		// 给templateTR包上z:list
		wrapTemplateTR(templateTable);
		String html = template.getOuterHTML();
		TemplateCompiler tc = new TemplateCompiler(ZhtmlManagerContext.getInstance());
		tc.compileSource(html);
		executor = tc.getExecutor();
	}

	public boolean wrapTemplateTR(HtmlElement parent) {
		for (int i = 0; i < parent.getChildren().size(); i++) {
			HtmlNode node = parent.getChildren().get(i);
			if (node == templateTR) {
				HtmlElement list = new HtmlElement("z:list");// 从上下文中获取item,data
				parent.getChildren().set(i, list);
				list.setParent(node.getParent());
				list.addChild(node);
				return true;
			} else if (node instanceof HtmlElement) {
				if (wrapTemplateTR((HtmlElement) node)) {
					return true;
				}
			}
		}
		return false;
	}

	public HtmlTable getTemplateTable() {
		return templateTable;
	}

	public List<HtmlTR> getSourceTRList() {
		return null;
	}

	public List<HtmlTR> getRewritedTRList() {
		return null;
	}

	public TemplateExecutor getExecutor() {
		return executor;
	}

	public HtmlTR getTemplateTR() {
		return templateTR;
	}

	public HtmlTR getHeadTR() {
		return headTR;
	}

	public HtmlTR getPageBarTR() {
		return pageBarTR;
	}

	public HtmlDocument getSource() {
		return source;
	}

	public HtmlDocument getTemplate() {
		return template;
	}

}
