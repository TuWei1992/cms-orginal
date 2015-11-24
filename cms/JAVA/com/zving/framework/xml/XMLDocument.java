package com.zving.framework.xml;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.utility.FastStringBuilder;

/**
 * 内存中的的XML文档。<br>
 * <br>
 * 关于简易路径：<br>
 * 本类的一些方法支持使用简易路径，所谓简易路径是指像"a.b.c"这样的字符串，<br>
 * 表示文档下的QName为a的节点下的QName为b的字节点下的QName为c的孙节点。<br>
 * 简易路径允许使用“*”作为通配符，例如"a.*.c"表示文档下的QName为a的节点下的所有QName为c的孙节点。
 * 如果简易路径在XMLElement的实例中使用，则是指相对于该元素的路径，不是指从根节点开始的路径。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-11
 */
public class XMLDocument {
	/**
	 * 默认XML文件编码为UTF-8
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";
	/**
	 * 默认XML版本为1.0
	 */
	public static final String DEFAULT_VERSION = "1.0";

	private XMLElement root;
	private List<XMLNode> children = new ArrayList<XMLNode>(2);
	private String encoding = DEFAULT_ENCODING;
	private String version = DEFAULT_VERSION;
	private String docType;

	/**
	 * @param path 简易路径
	 * @return 符合简易XML路径要求的所有元素
	 */
	public List<XMLElement> elements(String path) {
		String prefix = path;
		int index = path.indexOf(".");
		if (index > 0) {
			prefix = path.substring(0, index);
			path = path.substring(index + 1);
		}
		if (!"*".equals(prefix) && !root.getQName().equals(prefix)) {
			return new ArrayList<XMLElement>();// 返回空列表
		}
		return root.elements(path);
	}

	/**
	 * @return 文件编码
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * 设置文件编码
	 * 
	 * @param encoding 字符集
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * @return DOCTYPE声明
	 */
	public String getDocType() {
		return docType;
	}

	/**
	 * 设置DOCTYPE声明
	 * 
	 * @param docType DOCTYPE声明
	 */
	public void setDocType(String docType) {
		this.docType = docType;
	}

	/**
	 * 创建根节点
	 * 
	 * @param QName 根节点的QName
	 * @return 创建好的根节点
	 */
	public XMLElement createRoot(String QName) {
		root = new XMLElement(QName);
		children.add(root);
		return root;
	}

	/**
	 * 创建一个XML注释
	 * 
	 * @param comment 注释内容
	 */
	public void addComment(String comment) {
		children.add(new XMLComment(comment));
	}

	/**
	 * 创建一个XML指令
	 * 
	 * @param instruction 指令内容
	 */
	public void addInstruction(String instruction) {
		children.add(new XMLInstruction(instruction));
	}

	/**
	 * 将一个元素设为本文档的根元素
	 * 
	 * @param root XML元素
	 */
	public void setRoot(XMLElement root) {
		this.root = root;
		children.add(root);
	}

	/**
	 * @return XML文档的根元素
	 */
	public XMLElement getRoot() {
		return root;
	}

	/**
	 * @return XML版本
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * 设置XML版本号
	 * 
	 * @param version 版本号
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return 输出成XMl字符串
	 */
	public String asXML() {
		return toString();
	}

	@Override
	public String toString() {
		FastStringBuilder sb = new FastStringBuilder();
		sb.append("<?xml version=\"" + version + "\" encoding=\"" + encoding + "\"?>");
		if (docType != null) {
			sb.append("\n<!DOCTYPE ");
			sb.append(docType);
			sb.append(">");
		}
		for (XMLNode node : children) {
			sb.append("\n");
			node.toString("", sb);
		}
		return sb.toStringAndClose();
	}

	/**
	 * 在指定的简易路径下创建指定QName的节点，如果有多个路径符合要求，则创建到第一个路径下。如果路径中的各级节点不存在，则自动创建。
	 * 
	 * @param path 简易路径
	 * @param QName QName
	 * @return 创建的XML元素
	 */
	public XMLElement addElement(String path, String QName) {
		String[] arr = path.split("\\.");
		if (!root.getQName().equals(arr[0])) {
			return null;
		}
		XMLElement current = root;
		for (int i = 1; i < arr.length; i++) {
			String segment = arr[i];
			XMLElement ele = current.element(segment);
			if (ele == null) {
				ele = current.addElement(segment);
			}
			current = ele;
		}
		return current.addElement(QName);
	}

	/**
	 * 考虑到文档有可能常驻内存，则需要调用repack()重新组织字符串以节约内存
	 */
	void repack() {
		if (docType != null) {
			docType = new String(docType.toCharArray());
		}
		version = new String(version.toCharArray());
		encoding = new String(encoding.toCharArray());
		for (XMLNode node : children) {
			node.repack();
		}
	}
}
