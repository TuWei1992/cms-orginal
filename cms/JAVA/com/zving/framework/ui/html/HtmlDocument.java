package com.zving.framework.ui.html;

import com.zving.framework.utility.FastStringBuilder;

/**
 * 表示一个Html文档或者文档片段
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-11
 */
public class HtmlDocument extends HtmlElementContainer {
	@Override
	public int getType() {
		return DOCUMENT;
	}

	public void addElement(HtmlElement ele) {
		getChildren().add(ele);
	}

	public String toHtml() {// NO_UCD
		return toString();
	}

	@Override
	public String toString() {
		FastStringBuilder sb = new FastStringBuilder();
		for (HtmlNode node : getChildren()) {
			node.getOuterHTML(sb);
		}
		return sb.toStringAndClose();
	}

	@Override
	public HtmlDocument clone() {
		HtmlDocument doc = new HtmlDocument();
		for (HtmlNode node : getChildren()) {
			doc.getChildren().add(node.clone());
		}
		return doc;
	}

	@Override
	public String getText() {
		FastStringBuilder sb = new FastStringBuilder();
		for (HtmlNode node : getChildren()) {
			sb.append(node.getText());
			sb.append(" ");
		}
		return sb.toStringAndClose();
	}

	@Override
	public void format(FastStringBuilder sb, String prefix) {
		for (HtmlNode node : getChildren()) {
			node.format(sb, prefix);
		}
	}
}
