package com.zving.framework.ui.html;

import java.util.ArrayList;

import com.zving.framework.utility.FastStringBuilder;

/**
 * HTML节点虚拟类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-11
 */
public abstract class HtmlNode implements Cloneable {
	public static final int DOCUMENT = 1;
	public static final int TEXT = 2;
	public static final int ELEMENT = 3;
	public static final int INSTRUCTION = 4;
	public static final int COMMENT = 5;

	HtmlElement parent;

	public void getOuterHTML(FastStringBuilder sb) {
		format(sb, null);
	}

	public abstract String getText();

	public abstract int getType();

	abstract void format(FastStringBuilder sb, String prefix);

	abstract void repack();

	@Override
	public HtmlNode clone() {
		try {
			return (HtmlNode) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setParent(HtmlElementContainer parent) {
		if (this instanceof HtmlDocument) {
			throw new HtmlParseException("Can't add HtmlDocument as a child.");
		}
		if (parent.children == null) {
			parent.children = new ArrayList<HtmlNode>(2);
		}
		if (!parent.children.contains(this)) {
			parent.children.add(this);
		}
		if (parent instanceof HtmlElement) {
			this.parent = (HtmlElement) parent;
		}
	}

	public HtmlElement getParent() {
		return parent;
	}

	public HtmlElement getParentByTagName(String tagName) {// NO_UCD
		HtmlElement parent = getParent();
		while (true) {
			if (parent == null || parent.getTagName().equalsIgnoreCase(tagName)) {
				return parent;
			}
			parent = parent.getParent();
		}
	}

	public String getOuterHTML() {
		return toString();
	}

	@Override
	public String toString() {
		FastStringBuilder sb = new FastStringBuilder();
		getOuterHTML(sb);
		return sb.toStringAndClose();
	}
}
