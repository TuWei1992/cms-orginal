package com.zving.framework.xml;

/**
 * 表示一个XML命名空间
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-29
 */
public class XMLNamespace {
	String prefix;

	public XMLNamespace(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}
}
