package com.zving.framework.ui.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.FastStringBuilder;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.xml.XMLParser;
import com.zving.framework.xml.XMLParser.ElementAttribute;

/**
 * HTML元素
 * 
 * @Author 王育春
 * @Date 2007-6-21
 * @Mail wyuch@zving.com
 */
public class HtmlElement extends HtmlElementContainer {

	public static final String SINGLETON_ATTRIBUTE = "_ZVING_ATTRIBUTE_SINGLETON";

	protected String tagName;

	protected CaseIgnoreMapx<String, String> attributes;

	protected int startCharIndex;

	protected int endCharIndex;

	protected int lineNumber;

	protected boolean closed;// 仅解析时使用

	protected String innerHTML;

	public HtmlElement(String tagName) {
		this.tagName = tagName;
	}

	public String getAttribute(String attrName) {
		if (attributes == null) {
			return null;
		}
		return attributes.getString(attrName.toLowerCase());
	}

	public void removeAttribute(String attrName) {
		if (attributes == null) {
			return;
		}
		attributes.remove(attrName.toLowerCase());
	}

	public CaseIgnoreMapx<String, String> getAttributes() {
		if (attributes == null) {
			attributes = new CaseIgnoreMapx<String, String>();
		}
		return attributes;
	}

	public CaseIgnoreMapx<String, String> attributes() {
		if (attributes == null) {
			attributes = new CaseIgnoreMapx<String, String>();
		}
		return attributes;
	}

	public String attributeValue(String attrName) {
		if (attributes == null) {
			return null;
		}
		return attributes.get(attrName);
	}

	public void setAttribute(String attrName, String attrValue) {
		if (attributes == null) {
			attributes = new CaseIgnoreMapx<String, String>();
		}
		attributes.put(attrName, attrValue);
	}

	public void addAttribute(String attrName, String attrValue) {
		setAttribute(attrName, attrValue);
	}

	public boolean hasAttribute(String attrName) {
		return attributes != null && attributes.containsKey(attrName);
	}

	public String getClassName() {
		return getAttribute("class");
	}

	public void setClassName(String className) {
		addAttribute("class", className);
	}

	public Boolean hasClassName(String className) {
		String myClassName = getClassName();
		if (StringUtil.isEmpty(myClassName)) {
			return false;
		}
		myClassName = " " + myClassName + " ";
		return myClassName.indexOf(" " + className + " ") != -1;
	}

	public void addClassName(String className) {
		if (!hasClassName(className)) {
			String myClassName = getClassName();
			if (StringUtil.isEmpty(myClassName)) {
				setClassName(className);
				return;
			}
			setClassName(myClassName + " " + className);
		}
	}

	public String getID() {
		return getAttribute("id");
	}

	public void setID(String id) {
		addAttribute("id", id);
	}

	public String getStyle() {
		return getAttribute("style");
	}

	public void setStyle(String style) {
		addAttribute("style", style);
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getInnerHTML() {
		FastStringBuilder sb = new FastStringBuilder();
		getInnerHTML(sb, null);
		return sb.toStringAndClose();
	}

	void getInnerHTML(FastStringBuilder sb, String prefix) {
		if (innerHTML != null) {
			if (children == null || children.size() == 0) {
				sb.append(prefix == null ? innerHTML : innerHTML.trim());
			}
		}
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				children.get(i).format(sb, prefix);
			}
		}
	}

	public void setInnerHTML(String innerHTML) {
		setInnerHTML(innerHTML, true);
	}

	public void setInnerHTML(String innerHTML, boolean parseFlag) {
		getChildren().clear();
		if (parseFlag) {
			if (tagName.equalsIgnoreCase("script") || tagName.equalsIgnoreCase("style")) {
				addText(innerHTML);
			} else {
				parseInnerHTML(innerHTML);
			}
		} else {
			this.innerHTML = innerHTML;
		}
	}

	@Override
	public String toString() {
		return getOuterHTML();
	}

	/**
	 * 解析类似于html中的标签属性成一个map，可以允许有多个属性。<br>
	 * 属性值界定符兼容单引号、双引号， 属性值不含特列字符时也可以不用引号。<br>
	 * <br>
	 * 备注：将此方法放到HtmlElement是为了和以前的版本兼容。
	 */
	public static Mapx<String, Object> parseAttr(String attrs) {
		Mapx<String, Object> map = new CaseIgnoreMapx<String, Object>();
		int start = 0;
		List<Integer> list = new ArrayList<Integer>();
		char[] cs = attrs.toCharArray();
		start = XMLParser.ignoreSpace(cs, start);
		if (cs[start] == '<') {
			int index = -1;
			for (int i = start + 1; i < cs.length; i++) {
				if (XMLParser.isSpace(cs[i])) {
					index = i;
					break;
				}
			}
			if (index < 0) {
				return map;
			} else {
				start = index + 1;
			}
		}
		while (true) {
			ElementAttribute ea = HtmlParser.expectAttribute(cs, start, list);
			if (ea == null) {
				break;
			}
			map.put(ea.Name, ea.Value);
			start = ea.EndCharIndex;
		}
		return map;
	}

	public void parseHtml(String html) {
		HtmlParser parser = new HtmlParser(html);
		parser.parse();
		HtmlDocument doc = parser.getDocument();
		HtmlElement ele = null;
		for (HtmlNode node : doc.getChildren()) {
			if (node.getType() == HtmlNode.ELEMENT) {
				ele = (HtmlElement) node;
				break;
			}
		}
		if (ele == null) {
			throw new HtmlParseException("Not tag '" + getTagName() + "' found!");
		}
		if (ele.getTagName().equalsIgnoreCase(getTagName())) {
			attributes = ele.getAttributes();
			innerHTML = null;
			children = ele.children;
			if (children != null) {
				for (HtmlNode node : children) {
					node.parent = this;
				}
			}
		} else {
			throw new HtmlParseException("HtmlElement.parseHtml() failed,need a tag '" + getTagName() + "',but found a " + ele.getTagName()
					+ "!");
		}
	}

	void parseInnerHTML(String innerHTML) {
		if (innerHTML == null) {
			return;
		}
		if (innerHTML.indexOf('<') < 0) {
			addText(innerHTML);
			return;
		}
		HtmlParser parser = new HtmlParser(innerHTML);
		parser.parse();
		for (HtmlNode node : parser.getDocument().getChildren()) {
			addChild(node);
		}
	}

	public int getStartCharIndex() {
		return startCharIndex;
	}

	public int getEndCharIndex() {
		return endCharIndex;
	}

	@Override
	public String getText() {
		FastStringBuilder sb = new FastStringBuilder();
		if (children != null) {
			for (HtmlNode node : children) {
				if (node.getType() == HtmlNode.TEXT) {
					sb.append(node.getText());
					sb.append(" ");
				}
				if (node.getType() == HtmlNode.ELEMENT) {
					sb.append(node.getText());
				}
			}
		}
		return "";
	}

	@Override
	public int getType() {
		return HtmlNode.ELEMENT;
	}

	@Override
	public HtmlElement clone() {
		HtmlElement ele = (HtmlElement) super.clone();
		if (attributes != null) {
			ele.attributes = (CaseIgnoreMapx<String, String>) attributes.clone();
		}
		if (children != null) {
			ele.children = new ArrayList<HtmlNode>(children.size());
			for (HtmlNode node : children) {
				HtmlNode newNode = node.clone();
				newNode.setParent(ele);
			}
		}
		return ele;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void setStartCharIndex(int startCharIndex) {
		this.startCharIndex = startCharIndex;
	}

	public void setEndCharIndex(int endCharIndex) {
		this.endCharIndex = endCharIndex;
	}

	@Override
	void repack() {
		tagName = new String(tagName.toCharArray());
		CaseIgnoreMapx<String, String> map = new CaseIgnoreMapx<String, String>();
		if (attributes != null) {
			for (Entry<String, String> entry : attributes.entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				if (v.indexOf('\"') >= 0) {
					v = v.replace("\"", "&quot;");
				}
 				v = new String(v.toCharArray());
				k = new String(k.toCharArray());
				map.put(k, v);
			}
			attributes = map;
		}
		super.repack();
	}

	@Override
	void format(FastStringBuilder sb, String prefix) {// 如果prefix为null，则不换行
		if (prefix != null) {
			sb.append("\n");
			sb.append(prefix);
		}
		sb.append("<");
		sb.append(tagName);
		if (attributes != null) {
			for (String k : attributes.keySet()) {
				Object v = attributes.get(k);
				if (v != null) {
					sb.append(" ");
					sb.append(k);
					String str = v.toString();
					if (!SINGLETON_ATTRIBUTE.equals(str)) {
						char literal = '\"';
						if (str.indexOf(literal) >= 0) {// 如果里面有双引号，则换成单引号。
							literal = '\'';
						}
						sb.append("=");
						sb.append(literal);
						sb.append(v);
						sb.append(literal);
					}
				}
			}
		}
		if (innerHTML == null && getChildren().size() == 0 && ObjectUtil.in(tagName.toLowerCase(), HtmlParser.NO_CHILD_TAGS)) {
			sb.append(" />");
		} else {
			sb.append(">");
			getInnerHTML(sb, prefix == null ? null : prefix + "\t");
			if (prefix != null && children.size() > 0 && (children.size() > 1 || children.get(0).getType() == ELEMENT)) {
				sb.append("\n");
				sb.append(prefix);
			}
			sb.append("</");
			sb.append(tagName);
			sb.append(">");
		}
	}

}
