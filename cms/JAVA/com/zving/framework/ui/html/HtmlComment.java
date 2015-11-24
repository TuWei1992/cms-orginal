package com.zving.framework.ui.html;

import com.zving.framework.utility.FastStringBuilder;

/**
 * HTML中的注释
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-14
 */
public class HtmlComment extends HtmlNode {
	String comment;

	public HtmlComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public int getType() {
		return HtmlNode.COMMENT;
	}

	@Override
	void repack() {
		comment = new String(comment.toCharArray());
	}

	@Override
	public void format(FastStringBuilder sb, String prefix) {
		sb.append("<!--").append(comment).append("-->");
	}

	@Override
	public HtmlNode clone() {
		return new HtmlComment(comment);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
