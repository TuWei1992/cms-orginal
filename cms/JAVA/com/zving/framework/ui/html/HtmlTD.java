package com.zving.framework.ui.html;

import com.zving.framework.utility.FastStringBuilder;

/**
 * Html中的td标签<br>
 * 注意：需兼容TH/TD
 * 
 * @Author 王育春
 * @Date 2007-6-21
 * @Mail wyuch@zving.com
 */
public class HtmlTD extends HtmlElement {
	boolean isTH;

	public HtmlTD() {
		super("td");
	}

	public HtmlTD(HtmlElement ele) {
		super("td");
		if (!ele.getTagName().equalsIgnoreCase("td") && !ele.getTagName().equalsIgnoreCase("th")) {
			throw new HtmlParseException("Element can't convert to a td,tag=" + ele.getTagName());
		}
		attributes = ele.attributes;
		children = ele.children;
		isTH = ele.getTagName().equalsIgnoreCase("th");
		if (children != null) {
			for (HtmlNode node : children) {
				node.parent = this;
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

	public String getVAlign() {
		return attributes.get("vAlign");
	}

	public void setVAlign(String vAlign) {
		attributes.put("vAlign", vAlign);
	}

	public void setColSpan(String colSpan) {
		addAttribute("colspan", colSpan);
	}

	public String getColSpan() {
		return getAttribute("colspan");
	}

	public void setRowSpan(String rowSpan) {
		addAttribute("rowSpan", rowSpan);
	}

	public String getRowSpan() {
		return getAttribute("rowSpan");
	}

	@Override
	public HtmlTR getParent() {
		return (HtmlTR) parent;
	}

	public boolean isHead() {
		return tagName.equalsIgnoreCase("th");
	}

	public void setHead(boolean isHead) {
		if (isHead) {
			tagName = "th";
		} else {
			tagName = "tr";
		}
	}

	/**
	 * 返回所属tr。<br>
	 * 本方法和getParent()的返回值有可能不同，是因为有些场合td的父标签不一定是tr。
	 */
	public HtmlTR getTR() {
		HtmlElement parent = getParent();
		while (true) {
			if (parent == null) {
				return null;
			}
			if (parent instanceof HtmlTR) {
				return (HtmlTR) parent;
			}
			parent = parent.getParent();
		}
	}

	@Override
	public HtmlTD clone() {
		return new HtmlTD(super.clone());
	}

	public boolean isTH() {
		return isTH;
	}

	public void setTH(boolean isTH) {
		this.isTH = isTH;
	}

	@Override
	void format(FastStringBuilder sb, String prefix) {// 如果prefix为null，则不换行
		String oldTagName = tagName;
		tagName = isTH ? "th" : oldTagName;
		super.format(sb, prefix);
		tagName = oldTagName;
	}
}
