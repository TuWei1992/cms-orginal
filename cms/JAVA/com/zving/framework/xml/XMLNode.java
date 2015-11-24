package com.zving.framework.xml;

import java.util.ArrayList;

import com.zving.framework.utility.FastStringBuilder;
import com.zving.framework.utility.StringUtil;

/**
 * 表示一个XML节点的虚拟类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-11
 */
public abstract class XMLNode {
	public static final int CDATA = 1;
	public static final int TEXT = 2;
	public static final int ELEMENT = 3;
	public static final int INSTRUCTION = 4;
	public static final int COMMENT = 5;
	XMLElement parent;

	/**
	 * 将节点作为字符串输出到一个FastStringBuilder
	 * 
	 * @param prefix 缩进前缀
	 * @param sb
	 */
	public abstract void toString(String prefix, FastStringBuilder sb);

	/**
	 * @return 节点内部的文本
	 */
	public abstract String getText();

	/**
	 * @return 节点类型
	 */
	public abstract int getType();

	/**
	 * 如果节点需要常驻内存，则需要调用repack()重新组织字符串以节约内存
	 */
	abstract void repack();

	/**
	 * 设置节点的父元素
	 * 
	 * @param parent 父元素
	 */
	public void setParent(XMLElement parent) {
		this.parent = parent;
		if (parent.children == null) {
			parent.children = new ArrayList<XMLNode>(4);
		}
		this.parent.children.add(this);
	}

	/**
	 * 字符串编码
	 */
	protected void encode(String value, FastStringBuilder sb) {
		if (StringUtil.isEmpty(value)) {
			return;
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '<') {
				sb.append("&lt;");
			} else if (c == '>') {
				sb.append("&gt;");
			} else if (c == '\"') {
				sb.append("&quot;");
			} else if (c == '\'') {
				sb.append("&apos;");
			} else if (c == '&') {
				sb.append("&amp;");
			} else {
				sb.append(c);
			}
		}
	}

	/**
	 * @return 节点的父元素
	 */
	public XMLElement getParent() {
		return parent;
	}

	/**
	 * @return 输出节点成XML字符串
	 */
	public String getXML() {
		return toString();
	}

	@Override
	public String toString() {
		FastStringBuilder sb = new FastStringBuilder();
		toString("", sb);
		return sb.toStringAndClose();
	}
}
