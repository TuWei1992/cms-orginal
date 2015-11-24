package com.zving.framework.ui.html;

import java.util.ArrayList;
import java.util.List;

/**
 * Html中的tr元素
 * 
 * @Author 王育春
 * @Date 2007-6-21
 * @Mail wyuch@zving.com
 */
public class HtmlTR extends HtmlElement {
	public HtmlTR() {
		super("tr");
	}

	public HtmlTR(HtmlElement ele) {
		super("tr");
		if (!ele.getTagName().equalsIgnoreCase("tr")) {
			throw new HtmlParseException("Element can't convert to a tr,tag=" + ele.getTagName());
		}
		attributes = ele.attributes;
		children = ele.children;
		if (children != null) {
			for (HtmlNode node : children) {
				node.parent = this;
			}
		}
		convertTD(this);
	}

	public void addTD(HtmlTD td) {// NO_UCD
		addChild(td);
	}

	private void convertTD(HtmlElement parent) {
		for (int i = 0; i < parent.getChildren().size(); i++) {
			HtmlNode node = parent.children.get(i);
			if (node.getType() == HtmlNode.ELEMENT) {
				HtmlElement ele = (HtmlElement) node;
				if (ele instanceof HtmlTD) {
					continue;
				}
				if (ele.getTagName().equalsIgnoreCase("td") || ele.getTagName().equalsIgnoreCase("th")) {
					HtmlTD td = new HtmlTD(ele);
					td.parent = parent;
					parent.children.set(i, td);
				} else {
					convertTD(ele);
				}
			}
		}
	}

	public HtmlTD getTD(int index) {
		int i = 0;
		convertTD(this);
		for (HtmlNode node : getChildren()) {
			if (node instanceof HtmlTD) {
				if (i == index) {
					return (HtmlTD) node;
				}
				i++;
			}
		}
		throw new RuntimeException("getTD() Index out of range:" + index + ",max is " + i);
	}

	public void removeTD(int index) {
		HtmlTD TD = getTD(index);// 隐含调用了convertTD()
		children.remove(TD);
	}

	public List<HtmlTD> getTDList() {
		convertTD(this);
		List<HtmlElement> tds = getTopElementsByTagName("td");
		ArrayList<HtmlTD> list = new ArrayList<HtmlTD>(tds.size());
		for (HtmlElement ele : tds) {
			list.add((HtmlTD) ele);
		}
		return list;
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

	public String getVAlign() {
		return attributes.get("vAlign");
	}

	public void setVAlign(String vAlign) {
		attributes.put("vAlign", vAlign);
	}

	/**
	 * 返回所属table。<br>
	 * 本方法和getParent()的返回值有可能不同，是因为有些场合tr的父标签不一定是table。
	 */
	public HtmlTable getTable() {
		HtmlElement parent = getParent();
		while (true) {
			if (parent == null) {
				return null;
			}
			if (parent instanceof HtmlTable) {
				return (HtmlTable) parent;
			}
			parent = parent.getParent();
		}
	}

	@Override
	public HtmlTR clone() {
		return new HtmlTR(super.clone());
	}
}
