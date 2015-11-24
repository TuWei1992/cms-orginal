package com.zving.framework.ui.html;

import com.zving.framework.utility.FastStringBuilder;

/**
 * Html中的文本
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-11
 */
public final class HtmlText extends HtmlNode {
	String text;

	public HtmlText(String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public int getType() {
		return HtmlNode.TEXT;
	}

	@Override
	void repack() {
		if (text.indexOf('<') >= 0) {
			text = text.replace("<", "&lt;");
		}
		if (text.indexOf('>') >= 0) {
			text = text.replace(">", "&gt;");
		}
		text = new String(text.toCharArray());
	}

	@Override
	public void format(FastStringBuilder sb, String prefix) {
		if (text != null) {
			sb.append(prefix == null ? text : text.trim());
		}
	}

	@Override
	public HtmlNode clone() {
		return new HtmlText(text);
	}

	public void setText(String text) {
		this.text = text;
	}

}
