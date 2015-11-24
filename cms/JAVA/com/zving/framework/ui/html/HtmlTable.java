package com.zving.framework.ui.html;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.utility.FastStringBuilder;

/**
 * Html中的table元素
 * 
 * @Author 王育春
 * @Date 2007-6-21
 * @Mail wyuch@zving.com
 */
public class HtmlTable extends HtmlElement {
	private boolean TBody = false;

	private boolean THead = false;

	public HtmlTable() {
		super("table");
	}

	public HtmlTable(HtmlElement ele) {
		super("table");
		if (!ele.getTagName().equalsIgnoreCase("table")) {
			throw new HtmlParseException("Element can't convert to a table,tag=" + ele.getTagName());
		}
		attributes = ele.attributes;
		children = ele.children;
		if (children != null) {
			for (HtmlNode node : children) {
				node.parent = this;
			}
		}
		convertTR(this);
	}

	@Override
	public void parseHtml(String html) {
		super.parseHtml(html);
		convertTR(this);
	}

	@Override
	void parseInnerHTML(String html) {
		super.parseInnerHTML(html);
		convertTR(this);
	}

	public void addTR(HtmlTR tr) {
		addChild(tr);
	}

	private void convertTR(HtmlElement parent) {
		for (int i = 0; i < parent.getChildren().size(); i++) {
			HtmlNode node = parent.children.get(i);
			if (node.getType() == HtmlNode.ELEMENT) {
				HtmlElement ele = (HtmlElement) node;
				if (ele instanceof HtmlTR) {
					continue;
				}
				if (ele.getTagName().equalsIgnoreCase("tr")) {
					HtmlTR tr = new HtmlTR(ele);
					tr.parent = parent;
					parent.children.set(i, tr);
				} else {
					convertTR(ele);
				}
			}
		}
	}

	public HtmlTR getTR(int index) {
		int i = 0;
		convertTR(this);
		for (HtmlNode node : getChildren()) {
			if (node instanceof HtmlTR) {
				if (i == index) {
					return (HtmlTR) node;
				}
				i++;
			}
		}
		throw new RuntimeException("getTR() Index out of range:" + index + ",max is " + i);
	}

	public void removeTR(int index) {
		HtmlTR tr = getTR(index);// 隐含调用了convertTR()
		children.remove(tr);
	}

	public List<HtmlTR> getTRList() {
		convertTR(this);
		List<HtmlElement> trs = getTopElementsByTagName("tr");
		ArrayList<HtmlTR> list = new ArrayList<HtmlTR>(trs.size());
		for (HtmlElement ele : trs) {
			list.add((HtmlTR) ele);
		}
		return list;
	}

	public void removeColumn(int index) {// NO_UCD
		for (int i = 0; i < getChildren().size(); i++) {
			HtmlTR tr = getTR(i);
			if (index < tr.getChildren().size()) {
				tr.removeTD(index);
			}
		}
	}

	public void setWidth(int width) {
		attributes.put("width", width + "");
	}

	public int getWidth() {
		return attributes.getInt("width");
	}

	public void setHeight(int height) {
		attributes.put("height", height + "");
	}

	public int getHeight() {
		return attributes.getInt("height");
	}

	public void setAlign(String align) {
		attributes.put("align", align);
	}

	public String getAlign() {
		return attributes.get("align");
	}

	public void setBgColor(String bgColor) {
		attributes.put("bgColor", bgColor);
	}

	public String getBgColor() {
		return attributes.get("bgColor");
	}

	public void setBackgroud(String backgroud) {
		attributes.put("backgroud", backgroud);
	}

	public String getBackgroud() {
		return attributes.get("backgroud");
	}

	public void setCellSpacing(String cellSpacing) {
		addAttribute("cellSpacing", cellSpacing);
	}

	public String getCellSpacing() {
		return getAttribute("cellSpacing");
	}

	public void setCellPadding(String cellPadding) {
		addAttribute("cellPadding", cellPadding);
	}

	public String getCellPadding() {
		return getAttribute("cellPadding");
	}

	public boolean hasTBody() {// NO_UCD
		return TBody;
	}

	public void setTBody(boolean tBody) {
		TBody = tBody;
	}

	public boolean hasTHead() {// NO_UCD
		return THead;
	}

	public void setTHead(boolean tHead) {
		THead = tHead;
	}

	@Override
	void getInnerHTML(FastStringBuilder sb, String prefix) {
		FastStringBuilder outsideOfTbodyNodes = new FastStringBuilder();
		// tbody里只放tr标签，例如script等标签，要自动溢出到tbody标签外
		if (innerHTML != null) {
			if (children == null || children.size() == 0) {
				sb.append(innerHTML);
			}
		}
		boolean first = true;
		convertTR(this);
		if (children != null && children.size() > 0) {
			if (THead) {
				sb.append(prefix == null ? "" : "\n" + prefix);
				sb.append("<thead>");
			}
			for (HtmlNode node : children) {
				if (node instanceof HtmlElement && ((HtmlElement) node).tagName == "script") {
					node.format(outsideOfTbodyNodes, prefix);
				} else {
					node.format(sb, prefix);
				}
				if (THead && node instanceof HtmlTR) {
					if (first) {
						sb.append(prefix == null ? "" : "\n" + prefix);
						sb.append("</thead>");
						if (TBody) {
							sb.append(prefix == null ? "" : "\n" + prefix);
							sb.append("<tbody>");
						}
					}
					first = false;
				}
			}
			if (TBody) {
				sb.append(prefix == null ? "" : "\n" + prefix);
				sb.append("</tbody>");
			}
			sb.append(outsideOfTbodyNodes.toString());
		}
	}

	public void addTHead() {// NO_UCD
		convertTR(this);
		addTHead(this);
	}

	private void addTHead(HtmlElement parent) {
		for (int i = 0; i < parent.getChildren().size(); i++) {
			HtmlNode node = parent.children.get(i);
			if (node instanceof HtmlTR) {
				HtmlElement ele = new HtmlElement("thead");
				ele.parent = parent;
				parent.children.set(i, ele);
				ele.addChild(node);
				break;
			} else {
				HtmlElement ele = (HtmlElement) node;
				addTHead(ele);
			}
		}
	}

	@Override
	public HtmlTable clone() {
		return new HtmlTable(super.clone());
	}
}
