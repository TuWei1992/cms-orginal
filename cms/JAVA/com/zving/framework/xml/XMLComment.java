package com.zving.framework.xml;

import com.zving.framework.utility.FastStringBuilder;

/**
 * XML中的一段注释
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-14
 */
public class XMLComment extends XMLNode {
	String comment;

	public XMLComment(String comment) {
		this.comment = comment;
	}

	@Override
	public void toString(String prefix, FastStringBuilder sb) {
		sb.append(prefix).append("<!--").append(comment).append("-->");
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public int getType() {
		return XMLNode.COMMENT;
	}

	@Override
	void repack() {
		comment = new String(comment.toCharArray());
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
