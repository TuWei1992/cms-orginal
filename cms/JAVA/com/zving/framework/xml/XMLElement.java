package com.zving.framework.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.FastStringBuilder;
import com.zving.framework.utility.ObjectUtil;

/**
 * 表示一个XML元素
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-10
 */
public final class XMLElement extends XMLNode {
	int lineNumber;

	int startCharIndex;

	int endCharIndex;

	Mapx<String, String> attributes = null;

	String QName;

	List<XMLNode> children;

	int pos;// 解析时有值，手动new时无值

	/**
	 * 构造器
	 * 
	 * @param QName 元素的QName
	 */
	public XMLElement(String QName) {
		this.QName = QName;
	}

	/**
	 * 添加子元素
	 * 
	 * @param QName 子元素的QName
	 * @return 添加的子元素
	 */
	public XMLElement addElement(String QName) {
		XMLElement node = new XMLElement(QName);
		node.setParent(this);
		return node;
	}

	/**
	 * 添加文本块
	 * 
	 * @param text 文本
	 */
	public void addText(String text) {
		XMLText node = new XMLText(text);
		node.setParent(this);
	}

	/**
	 * 添加CDATA块
	 * 
	 * @param text CDATA文本
	 */
	public void addCDATA(String text) {
		XMLCDATA node = new XMLCDATA(text);
		node.setParent(this);
	}

	/**
	 * 添加注释
	 * 
	 * @param comment 注释
	 */
	public void addComment(String comment) {
		XMLComment node = new XMLComment(comment);
		node.setParent(this);
	}

	/**
	 * 添加指令
	 * 
	 * @param instruction 指令
	 */
	public void addInstruction(String instruction) {
		XMLInstruction node = new XMLInstruction(instruction);
		node.setParent(this);
	}

	@Override
	public XMLElement getParent() {
		return parent;
	}

	/**
	 * @return 元素的属性列表
	 */
	public Mapx<String, String> getAttributes() {
		return attributes();
	}

	/**
	 * @return 元素的属性列表
	 */
	public Mapx<String, String> attributes() {
		if (attributes == null) {
			attributes = new Mapx<String, String>();
		}
		return attributes;
	}

	/**
	 * @param attrName 属性名
	 * @return 指定属性名对应的属性值
	 */
	public String attributeValue(String attrName) {
		if (attributes == null) {
			return null;
		}
		return attributes.get(attrName);
	}

	/**
	 * 增加一个属性值
	 * 
	 * @param attrName 属性名
	 * @param attrValue 属性值
	 */
	public void addAttribute(String attrName, String attrValue) {
		if (attributes == null) {
			attributes = new Mapx<String, String>();
		}
		attributes.put(attrName, attrValue);
	}

	/**
	 * @return 元素的QName
	 */
	public String getQName() {
		return QName;
	}

	/**
	 * @return 当前元素在xml字符串中的起始位置。
	 */
	public int getPosition() {
		return pos;
	}

	/**
	 * 设置元素的内部文本。如果元素下已经有其他子节点，则会删除其他子节点。
	 * 
	 * @param text 文本
	 */
	public void setText(String text) {
		if (children == null) {
			children = new ArrayList<XMLNode>();
		} else {
			children.clear();
		}
		addText(text);
	}

	@Override
	public String getText() {
		if (children == null) {
			return "";
		}
		FastStringBuilder sb = new FastStringBuilder();
		for (XMLNode node : children) {
			if (ObjectUtil.in(node.getType(), XMLNode.TEXT, XMLNode.CDATA)) {
				sb.append(node.getText());
			}
		}
		return sb.toStringAndClose();
	}

	/**
	 * @return 所有子元素
	 */
	public List<XMLElement> elements() {
		List<XMLElement> elements = new ArrayList<XMLElement>();
		if (children == null) {
			return elements;
		}
		for (XMLNode node : children) {
			if (node.getType() == XMLNode.ELEMENT) {
				elements.add((XMLElement) node);
			}
		}
		return elements;
	}

	/**
	 * @return 所有子元素
	 */
	public List<XMLElement> getElements() {
		return elements();
	}

	/**
	 * @param list 元素列表
	 * @param QName 指定的QName，如果值为*，则返回所有子元素
	 * @return 返回元素列表中所有元素的QName等于指定值的子元素的集合
	 */
	private List<XMLElement> elements(List<XMLElement> list, String QName) {
		List<XMLElement> result = new ArrayList<XMLElement>();
		for (XMLElement node : list) {
			for (XMLNode child : node.getChildren()) {
				if (child.getType() != ELEMENT) {
					continue;
				}
				XMLElement ele = (XMLElement) child;
				if ("*".equals(QName) || ele.getQName().equalsIgnoreCase(QName)) {
					result.add(ele);
				}
			}
		}
		return result;
	}

	/**
	 * @param path 简易路径，见XMLDocument中关于简易路径的说明。
	 * @return 本元素下符合简易路径要求的所有子孙元素
	 */
	public List<XMLElement> elements(String path) {
		String[] arr = path == null ? new String[0] : path.split("\\.");
		List<XMLElement> list = new ArrayList<XMLElement>();
		list.add(this);
		for (String element2 : arr) {
			list = elements(list, element2);
			if (list == null || list.size() == 0) {
				break;
			}
		}
		return list;
	}

	/**
	 * @param path 简易路径，见XMLDocument中关于简易路径的说明。
	 * @return 子孙元素中符合指定简易路径的第一个元素的内部文本
	 */
	public String elementText(String path) {
		XMLElement child = element(path);
		if (child == null) {
			return "";
		}
		return child.getText();
	}

	/**
	 * @param qname
	 * @return 本元素中符合指定QName的第一个子孙元素
	 */
	public String elementText(XMLQName qname) {
		return elementText(qname.getQualifiedName());
	}

	/**
	 * @param qname
	 * @return 本元素中符合指定QName的所有子孙元素
	 */
	public List<XMLElement> elements(XMLQName qname) {
		return elements(qname.getQualifiedName());
	}

	/**
	 * @param path 简易路径，见XMLDocument中关于简易路径的说明。
	 * @return 本元素下符合简易路径要求的第一个子孙元素
	 */
	public XMLElement element(String path) {
		List<XMLElement> nodes = elements(path);
		return nodes == null || nodes.size() == 0 ? null : nodes.get(0);
	}

	/**
	 * @param qname
	 * @return 本元素下符合QName要求的第一个子孙元素
	 */
	public XMLElement element(XMLQName qname) {
		return element(qname.getQualifiedName());
	}

	/**
	 * 返回指定简易路径下属性值等指定参数的元素列表
	 * 
	 * @param path 简易路径，见XMLDocument中关于简易路径的说明。
	 * @param attrName 属性名
	 * @param attrValue 属性值
	 * @return XML元素列表
	 */
	public List<XMLElement> elementsByAttribute(String path, String attrName, String attrValue) {
		List<XMLElement> nodes = elements(path);
		if (nodes == null || nodes.size() == 0) {
			return null;
		}
		List<XMLElement> result = new ArrayList<XMLElement>();
		for (XMLElement n : nodes) {
			if (attrValue == null) {
				if (n.getAttributes().get(attrName) == null) {
					result.add(n);
				}
			} else if (attrValue.equals(n.getAttributes().get(attrName))) {
				result.add(n);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		FastStringBuilder sb = new FastStringBuilder();
		toString("", sb);
		return sb.toStringAndClose();
	}

	@Override
	public void toString(String prefix, FastStringBuilder sb) {
		sb.append(prefix);
		sb.append("<");
		sb.append(QName);
		if (attributes != null) {
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				sb.append(" ");
				sb.append(entry.getKey());
				sb.append("=\"");
				encode(entry.getValue(), sb);
				sb.append("\"");
			}
		}
		if (children == null || children.size() == 0) {
			sb.append(" />");
		} else {
			sb.append(">");
			if (children.size() == 1 && children.get(0).getType() == XMLNode.TEXT) {
				children.get(0).toString("", sb);
				sb.append("</");
				sb.append(QName);
				sb.append(">");
			} else {
				for (XMLNode child : children) {
					sb.append("\n");
					child.toString(prefix + "\t", sb);
				}
				sb.append("\n");
				sb.append(prefix);
				sb.append("</");
				sb.append(QName);
				sb.append(">");
			}
		}
	}

	@Override
	public int getType() {
		return XMLNode.ELEMENT;
	}

	/**
	 * 删除一个XML元素
	 * 
	 * @param ele XML元素
	 * @return 是否删除成功
	 */
	public boolean remove(XMLElement ele) {// NO_UCD
		for (int i = 0; children != null && i < children.size(); i++) {
			if (children.get(i) == ele) {
				children.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回元素在源文件中的行数，只在解析时有值
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * 设置元素在源文件中的行数
	 * 
	 * @param lineNumber 行数
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * 在指定的路径下增加指定QName的节点，如果有多个路径符合要求，则加到第一个路径下。如果路径中的各级节点不存在，则自动创建。
	 * 
	 * @param path 简易路径，见XMLDocument中关于简易路径的说明。
	 * @param QName QName
	 * @return 添加后的元素
	 */
	public XMLElement addElement(String path, String QName) {// NO_UCD
		String[] arr = path.split("\\.");
		XMLElement current = this;
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
	 * @return 所有子节点
	 */
	public List<XMLNode> getChildren() {
		if (children == null) {
			children = new ArrayList<XMLNode>(4);
		}
		return children;
	}

	/**
	 * @return 返回内部所有子节点的XML
	 */
	public String getInnerXML() {
		FastStringBuilder sb = new FastStringBuilder();
		if (children != null && children.size() > 0) {
			for (XMLNode node : children) {
				sb.append(node.getXML());
			}
		} else {
			return "";
		}
		return sb.toStringAndClose();
	}

	/**
	 * @return 元素在XML源代码中的起始位置，只有解析时有值
	 */
	public int getStartCharIndex() {
		return startCharIndex;
	}

	/**
	 * 设置元素的起始位置
	 * 
	 * @param startCharIndex 起始位置
	 */
	public void setStartCharIndex(int startCharIndex) {
		this.startCharIndex = startCharIndex;
	}

	/**
	 * @return 元素在XML源代码中的结束位置，只有解析时有值
	 */
	public int getEndCharIndex() {
		return endCharIndex;
	}

	/**
	 * 设置结束位置
	 * 
	 * @param endCharIndex 结束位置
	 */
	public void setEndCharIndex(int endCharIndex) {
		this.endCharIndex = endCharIndex;
	}

	@Override
	void repack() {
		QName = new String(QName.toCharArray());
		Mapx<String, String> map = new Mapx<String, String>();
		if (attributes != null) {
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				v = new String(v.toCharArray());
				k = new String(k.toCharArray());
				map.put(k, v);
			}
			attributes = map;
		}
		if (children != null) {
			for (XMLNode node : children) {
				node.repack();
			}
		}
	}

}
