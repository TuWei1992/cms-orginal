package com.zving.framework.xml;

import com.zving.framework.utility.FastStringBuilder;

/**
 * 表示一个XML文本块
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-11
 */
public final class XMLText extends XMLNode {
	String text;

	public XMLText(String text) {
		this.text = text;
	}

	@Override
	public void toString(String prefix, FastStringBuilder sb) {
		if (text != null) {
			encode(text, sb);
		}
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public int getType() {
		return XMLNode.TEXT;
	}

	@Override
	void repack() {
		text = new String(text.toCharArray());
	}

	public void setText(String text) {
		this.text = text;
	}

}
