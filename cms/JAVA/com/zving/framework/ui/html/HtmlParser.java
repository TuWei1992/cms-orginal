package com.zving.framework.ui.html;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.xml.XMLParser;
import com.zving.framework.xml.XMLParser.ElementAttribute;

/**
 * 一个简单而快速的 Html解析器，支持自定义标签，支持在标签属性中使用${expr}表达式。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-13
 */
public class HtmlParser {
	public static final String[] NO_CHILD_TAGS = new String[] { "script", "style", "br", "img", "input", "iframe", "hr", "meta", "link",
			"area", "basefont", "base", "bgsound", "embed", "isindex" };

	HtmlDocument doc = null;

	HtmlElement current = null;

	String html;

	int lastText = 0;

	char[] cs;

	String tag;

	List<Integer> lineNumList = new ArrayList<Integer>(128);

	public HtmlParser(String html) {
		this.html = html;
		cs = html.toCharArray();// this code can improve performance
	}

	public HtmlDocument parse() {
		try {
			lastText = 0;
			current = null;
			doc = new HtmlDocument();
			lineNumList.clear();
			int pos = 0;
			while (true) {
				int i = expectAny(pos);
				if (i < 0) {
					break;
				} else {
					pos = i;
				}
			}
			for (int i = doc.getChildren().size() - 1; i >= 0; i--) {
				HtmlNode node = doc.getChildren().get(i);
				if (node.getType() == HtmlNode.ELEMENT) {
					HtmlElement e = (HtmlElement) node;
					checkNotClosed(e);
					break;
				}
			}
			doc.repack();
			return doc;
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	void checkNotClosed(HtmlElement e) {
		if (e.closed) {
			return;
		}
		if (!ObjectUtil.in(e.getTagName().toLowerCase(), NO_CHILD_TAGS)) {
			e.setEndCharIndex(cs.length);
		} else {
			if (e.getParent() != null) {
				e.getParent().getChildren().addAll(e.getChildren());
			} else {
				doc.getChildren().addAll(e.getChildren());
			}
			e.getChildren().clear();
		}
		for (HtmlElement child : e.elements()) {
			checkNotClosed(child);
		}
		e.closed = true;
	}

	int expectAny(int pos) {
		int i = -1;
		for (int k = pos; k < cs.length; k++) {
			if (cs[k] == '<') {
				i = k;
				break;
			} else if (cs[k] == '$' && k < cs.length - 1 && cs[k + 1] == '{') {
				i = k;
				break;
			}
		}
		if (i >= 0 && cs[i] == '$') {// 跳过正文中的EL
			int i2 = expectExpression(cs, i);
			if (i2 > 0) {
				return i2;
			} else {
				return i + 2;
			}
		}
		if (i < 0 || i == cs.length - 1) {
			int start = lastText;
			int end = i < 0 ? cs.length : cs.length - 1;
			if (end > start) {
				String text = i < 0 ? html.substring(lastText) : html.substring(lastText, cs.length - 1);
				if (current == null) {
					doc.addText(text);
				} else {
					current.addText(text);
				}
			}
			return -1;
		}
		char c = cs[i + 1];
		if (c == '!' && XMLParser.expect(cs, i + 2, "--") == i + 4) {// 注释
			int i2 = expectComment(i);
			pos = i2 > 0 ? i2 : pos;
		} else if (c == '%' || c == '!' || c == '#' || c == '?') {// 指令
			int i2 = expectInstruction(i);
			pos = i2 > 0 ? i2 : pos;
		} else if (c == '/') {
			int i2 = expectElementEnd(i);
			pos = i2 > 0 ? i2 : pos;
		} else {
			int i2 = expectElement(i);
			pos = i2 > 0 ? i2 : pos;
		}
		return pos;
	}

	/**
	 * 期望一个表达式结束
	 */
	static int expectExpression(char[] cs, int pos) {
		char literal = 0;
		for (int j = pos + 2; j < cs.length; j++) {
			char h = cs[j];
			if (h == '\'' || h == '\"') {
				if (cs[j - 1] == '\\') {// 转义
					continue;
				}
				if (literal == 0) {
					literal = h;
				} else if (literal == h) {
					literal = 0;
				}
			} else if (h == '}' && cs[j - 1] != '\\') {
				if (literal == 0) {
					return j + 1;
				}
			} else if (h == '\n') {
				return -1;
			}
		}
		return -1;
	}

	int expectComment(int pos) {
		int i = XMLParser.indexOf(cs, "-->", pos);
		if (i > 0) {
			if (current == null) {
				if (lastText != pos) {
					doc.addText(html.substring(lastText, pos));
				}
				doc.addComment(html.substring(pos + 4, i));
			} else {
				if (lastText != pos) {
					current.addText(html.substring(lastText, pos));
				}
				current.addComment(html.substring(pos + 4, i));
			}
			return lastText = i + 3;
		} else {
			throw new HtmlParseException("Html comment not closed : line=" + XMLParser.getLineNum(lineNumList, cs, i));
		}
	}

	int expectInstruction(int pos) {
		int i = find(cs, pos, ">");
		if (i > 0) {
			if (current == null) {
				if (lastText != pos) {
					doc.addText(html.substring(lastText, pos));
				}
				doc.addInstruction(html.substring(pos, i + 1));
			} else {
				if (lastText != pos) {
					current.addText(html.substring(lastText, pos));
				}
				current.addInstruction(html.substring(pos, i + 1));
			}
			return lastText = i + 1;
		} else {
			throw new HtmlParseException("Html instruction not closed : line=" + XMLParser.getLineNum(lineNumList, cs, i));
		}
	}

	/**
	 * 从指定位置开始查找一个不处于字符串之中的字符
	 */
	public static int find(char[] cs, int pos, String str) {
		while (true) {
			if (pos >= cs.length) {
				return -1;
			}
			int i = XMLParser.indexOf(cs, str, pos);
			if (i == -1) {
				return -1;
			}
			char literal = 0;
			for (int j = i - 1; j >= pos; j--) {
				if (cs[j] == '\n') {
					if (literal != 0) {
						pos = i + 1;
					} else {
						return i;
					}
				} else if (cs[j] == '\'' && literal == '\'') {
					if (j == 0 || cs[j - 1] != '\\') {
						literal = 0;
					}
				} else if (cs[j] == '\"' && literal == '\"') {
					if (j == 0 || cs[j - 1] != '\\') {
						literal = 0;
					}
				}
			}
			if (pos != i + 1 && literal == 0) {// 说明前面没有换行
				return i;
			}
		}
	}

	int expectElement(int start) {
		int index = start + 1;
		for (int i = index; i < cs.length; i++) {
			char c = cs[i];
			if (XMLParser.isSpace(c) || c == '>') {
				index = i;
				break;
			}
		}
		tag = html.substring(start + 1, index);
		if (cs[index - 1] == '/') {
			tag = tag.substring(0, tag.length() - 1);
			index--;
		}
		if (index == start + 1 || XMLParser.isInvalidName(cs, start + 1, index)) {
			return start + 1;// 增加对小于号的容错性，如果小于后面不是正常的标签门则继续
		}

		if (lastText != start && current == null) {
			doc.addText(html.substring(lastText, start));
		}
		if (lastText != start && current != null) {
			current.addText(html.substring(lastText, start));
		}
		tryCloseNoChildElement(start);// 尝试关闭父标签

		HtmlElement ele = new HtmlElement(tag);
		ele.setLineNumber(XMLParser.getLineNum(lineNumList, cs, start));
		ele.setStartCharIndex(start);
		if (current == null) {
			doc.addElement(ele);
		} else {
			current.addChild(ele);
		}
		current = ele;
		start = index;

		// parse attributes
		while (true) {
			ElementAttribute ea = expectAttribute(cs, start, lineNumList);
			if (ea == null) {
				break;
			}
			if (ea.Name != null) {// 为null则表示有一些字符不合规被直接忽略了
				ele.addAttribute(ea.Name, ea.Value);
			}
			start = ea.EndCharIndex;
		}
		if ((index = XMLParser.expect(cs, start, "/>")) > 0) {
			current.setEndCharIndex(index);
			current.closed = true;
			current = current.getParent();
			lastText = index;
			return index;
		}
		index = XMLParser.expect(cs, start, ">");
		if (index < 0) {
			throw new HtmlParseException("Element not complete correctly: <" + tag + ",line number is "
					+ XMLParser.getLineNum(lineNumList, cs, start));
		}
		if (tag.equalsIgnoreCase("script") || tag.equalsIgnoreCase("style")) {// 特殊标签处理
			return lastText = expectSpecialEnd(index);
		}
		current.setEndCharIndex(index);
		return lastText = index;
	}

	/**
	 * 如果发现了新的标签但父标签又不允许有子标签，则尝试关闭父标签
	 */
	void tryCloseNoChildElement(int nextTagPosition) {
		if (current == null) {
			return;
		}
		String name = current.getTagName().toLowerCase();
		if (ObjectUtil.in(name, NO_CHILD_TAGS)) {
			HtmlElement e = current;
			current = current.getParent();
			if (current != null) {
				current.getChildren().addAll(e.getChildren());
			} else {
				doc.getChildren().addAll(e.getChildren());
			}
			e.getChildren().clear();
			e.closed = true;
		}
	}

	/**
	 * 尝试查找标签结束
	 */
	int expectElementEnd(int start) {
		int index = -1;
		for (int i = start + 2; i < cs.length; i++) {
			char c = cs[i];
			if (XMLParser.isSpace(c) || c == '>') {
				index = i;
				break;
			}
		}
		if (index < 0) {
			throw new HtmlParseException("Element not complete correctly, line number is " + XMLParser.getLineNum(lineNumList, cs, start));
		}
		if (current == null) {// 没有当前标签,则忽略
			return lastText = index + 1;
		}
		if (lastText != start) {
			current.addText(html.substring(lastText, start));
		}

		tag = html.substring(start + 2, index);
		int tmp = index;
		index = XMLParser.expect(cs, index, ">");
		if (index < 0 || XMLParser.isInvalidName(cs, start + 2, tmp)) {
			throw new HtmlParseException("Element not complete correctly,tag:" + tag + ", line number is "
					+ XMLParser.getLineNum(lineNumList, cs, start));
		}
		HtmlElement e = current;
		HtmlElement r = null;
		while (true) {// 先逐级查找有没有可以匹配的，没有匹配的就直接忽略。
			if (!e.getTagName().equalsIgnoreCase(tag)) {
				e = e.getParent();
				if (e == null) {
					break;
				}
			} else {
				r = e;
				break;
			}
		}
		e = current;
		if (r != null) {
			while (true) {
				if (e != r) {// 如果不是当前标签，则逐级关闭
					if (!ObjectUtil.in(e.getTagName().toLowerCase(), NO_CHILD_TAGS)) {
						e.setEndCharIndex(start);
					} else {
						if (e.getParent() != null) {
							e.getParent().getChildren().addAll(e.getChildren());
						} else {
							doc.getChildren().addAll(e.getChildren());
						}
						e.getChildren().clear();
					}
					e.closed = true;
					e = e.getParent();
					if (e == null) {
						break;
					}
				} else {
					e.closed = true;
					e.setEndCharIndex(index);
					current = e.getParent();
					break;
				}
			}
		}
		return lastText = index;
	}

	/**
	 * 尝试查找特殊标签的结尾，这些特殊标签内部含有长段文本，但不含有标签。例如style，script
	 */
	int expectSpecialEnd(int start) {
		int len = 2 + tag.length();
		int index = start;
		while (true) {
			if (index >= cs.length) {
				break;
			}
			index = XMLParser.indexOf(cs, "</", index);
			int end = index;
			if (index < 0 || index + len >= cs.length) {
				throw new HtmlParseException("Tag not complete correctly:" + tag + ", line number is "
						+ XMLParser.getLineNum(lineNumList, cs, index));
			}
			if (html.substring(index + 2, index + len).equalsIgnoreCase(tag)) {
				index += len;
				index = XMLParser.expect(cs, index, ">");
				if (index < 0) {
					throw new HtmlParseException("Tag not complete correctly:" + tag + ", line number is "
							+ XMLParser.getLineNum(lineNumList, cs, start));
				}
				current.addText(html.substring(start, end));
				current.setEndCharIndex(index);
				current.closed = true;
				current = current.getParent();
				break;
			} else {
				index += len;
			}
		}
		return index;
	}

	/**
	 * 试图解析一个标签属性，形如：
	 * 
	 * <pre>
	 * a = &quot;b&quot;
	 * </pre>
	 */
	public static ElementAttribute expectAttribute(char[] cs, int start, List<Integer> lineNumList) {
		if (start >= cs.length) {
			return null;
		}
		start = XMLParser.ignoreSpace(cs, start);
		if (cs[start] == '>' || cs[start] == '/' || cs[start] == '?') {
			return null;
		}

		// 处理单个EL作为属性的情况
		if (cs[start] == '$' && start < cs.length - 1 && cs[start + 1] == '{') {
			int i = expectExpression(cs, start);
			if (i > 0) {
				return new ElementAttribute(i, new String(cs, start, i - start), HtmlElement.SINGLETON_ATTRIBUTE);
			}
		}

		String attrName = null;
		String attrValue = null;
		int index = -1;
		for (int i = start; i < cs.length; i++) {
			char c = cs[i];
			if (c == '=') {
				index = i;
				break;
			} else if (c == '/' || c == '>') {// 没有等号并且标签直接结束
				index = i;
				attrName = new String(cs, start, index - start).trim();
				if (attrName.startsWith("${") && attrName.endsWith("}")) {
					attrValue = HtmlElement.SINGLETON_ATTRIBUTE;// 表示不成对的属性
					return new ElementAttribute(index, attrName, attrValue);
				} else {
					if (XMLParser.isInvalidName(cs, start, start + attrName.length())) {
						return new ElementAttribute(index, null, null);// 忽略
					} else {
						attrValue = attrName;
						return new ElementAttribute(index, attrName, attrValue);
					}
				}
			} else if (XMLParser.isSpace(c)) {
				i = XMLParser.ignoreSpace(cs, i + 1);
				if (cs[i] == '=') {
					index = i;
				} else {// 没有等号的属性，例如disabled
					index = i;
					String name = new String(cs, start, index - start).trim();
					if (name.startsWith("${") && name.endsWith("}")) {
						attrName = name;
						attrValue = HtmlElement.SINGLETON_ATTRIBUTE;// 表示不成对的属性
						return new ElementAttribute(index, attrName, attrValue);
					}
					if (XMLParser.isInvalidName(cs, start, start + name.length())) {
						return new ElementAttribute(index, null, null);// 忽略
					} else {
						attrName = name;
						attrValue = attrName;
						return new ElementAttribute(index, attrName, attrValue);
					}
				}
				break;
			}
		}
		if (index == -1) {
			throw new HtmlParseException("Tag not complete correctly, line number is " + XMLParser.getLineNum(lineNumList, cs, start));
		}
		String name = new String(cs, start, index - start).trim();
		if (XMLParser.isInvalidName(cs, start, start + name.length())) {
			return new ElementAttribute(index, null, null);// 忽略
		}
		start = index + 1;
		start = XMLParser.ignoreSpace(cs, start);
		char c = cs[start];
		if (c != '\"' && c != '\'') {
			// 属性值直接是一个表达式
			if (cs[start] == '$' && start < cs.length - 1 && cs[start + 1] == '{') {
				index = expectExpression(cs, start + 2);
				if (index < 0) {
					throw new HtmlParseException("Attribute '" + name + "' value is invalid, line number is "
							+ XMLParser.getLineNum(lineNumList, cs, start));
				}
			} else {
				for (int i = start; i < cs.length; i++) {
					if (XMLParser.isSpace(cs[i]) || cs[i] == '>' || (i < cs.length - 1 && cs[i] == '/' && cs[i + 1] == '>')) {
						attrValue = new String(cs, start, i - start);
						index = i;
						break;
					}
					if (cs[i] == '=') {// 有时候直接以等号结束属性
						attrValue = "";
						index = start;
						break;
					}
				}
			}
			index--;// 后面会再加1
		} else {
			start++;
			if (cs[start + 1] == '<') {// 标签属性中含有标签
				index = XMLParser.indexOf(cs, ">" + c, start);
				index++;
				attrValue = new String(cs, start, index - start);
			} else {
				// 属性值是一个表达式
				if (cs[start] == '$' && start < cs.length - 1 && cs[start + 1] == '{') {
					int i = expectExpression(cs, start + 2);
					if (i < 0) {
						index = XMLParser.indexOf(cs, c, start);
					} else {
						index = XMLParser.indexOf(cs, c, i);
					}
				} else {
					index = XMLParser.indexOf(cs, c, start);
				}
				if (index < 0) {
					throw new HtmlParseException("Attribute '" + name + "' value is invalid, line number is "
							+ XMLParser.getLineNum(lineNumList, cs, start));
				}
				attrValue = new String(cs, start, index - start);
			}
		}
		attrName = name;
		return new ElementAttribute(index + 1, attrName, attrValue);
	}

	/**
	 * 返回解析后的HtmlDocument实例
	 */
	public HtmlDocument getDocument() {
		return doc;
	}

}
