package com.zving.framework.xml;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.zving.framework.utility.FileUtil;

/**
 * 一个简单而快速的 XML解析器，需要将整个XML文档先载入内存并且不支持DTD/XSD校验。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-13
 */
public class XMLParser {
	/**
	 * XML文档
	 */
	XMLDocument doc = null;

	/**
	 * 当前正在解析的XML元素
	 */
	XMLElement current = null;

	/**
	 * XML源代码
	 */
	String xml;

	/**
	 * XML源代码对应的字符串数组
	 */
	char[] cs;

	/**
	 * 当前的元素标签名
	 */
	String tag;

	/**
	 * 当前CDATA块的值
	 */
	String cdata;

	/**
	 * 当前文本块的值
	 */
	String text;

	/**
	 * 存储文本中各个换行符的位置
	 */
	List<Integer> lineNumList = new ArrayList<Integer>(128);

	/**
	 * 构造器
	 * 
	 * @param xml 待解析的XML字符串
	 */
	public XMLParser(String xml) {
		this.xml = xml;
	}

	/**
	 * 构造器
	 * 
	 * @param is 待解析的输入流
	 */
	public XMLParser(InputStream is) {
		byte[] bs = FileUtil.readByte(is);
		try {
			xml = new String(bs, "UTF-8");
			cs = xml.toCharArray();
			int start = indexOf(cs, "encoding=", 0);
			if (start > 0) {
				start += 9;
				boolean isFinalQuote = true;
				for (int i = start; i < xml.length(); i++) {
					char chr = xml.charAt(i);
					if (Character.isSpaceChar(chr)) {
						start++;
					} else {
						if (chr == '\'' || chr == '\"') {
							start = i + 1;
							isFinalQuote = chr == '\"';
							break;
						}
					}
				}
				int end = isFinalQuote ? indexOf(cs, '\"', start) : indexOf(cs, '\'', start);
				if (end > 0) {
					String encoding = xml.substring(start, end).trim();
					if (!encoding.equalsIgnoreCase("UTF-8")) {
						xml = new String(bs, encoding);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new XMLParseException(e);
		}
	}

	/**
	 * 解析XML文档
	 * 
	 * @return 解析得到的XMLDocument实例
	 */
	public XMLDocument parse() {
		doc = new XMLDocument();
		cs = xml.toCharArray();// this code can improve performance
		lineNumList.clear();
		int end = expect(cs, 0, "<?xml");
		int pos = 0;
		if (end > 0) {
			pos = end;
			while (true) {
				ElementAttribute ea = expectAttribute(cs, pos, lineNumList);
				if (ea == null) {
					break;
				}
				if (ea.Name.equals("version")) {
					doc.setVersion(ea.Value);
				}
				if (ea.Name.equals("encoding")) {
					doc.setEncoding(ea.Value);
				}
				pos = ea.EndCharIndex;
			}
			if ((end = expect(cs, pos, "?>")) < 0) {
				throw new XMLParseException("Invalid xml prolog!");
			}
			pos = end;
		}
		end = expectOthers(pos);
		pos = end > 0 ? end : pos;
		end = expect(cs, pos, "<!DOCTYPE");
		if (end > 0) {
			pos = end;
			end = expectGT(pos);
			if (end < 0) {
				throw new XMLParseException("DOCTYPE not complete correctly!");
			}
			doc.setDocType(xml.substring(pos, end - 1));
			pos = end;
		}
		end = expectOthers(pos);
		pos = end > 0 ? end : pos;// may be exists comments after DOCTYPE
		end = expectElement(pos);
		if (end > 0) {
			pos = end;
		}
		expectOthers(pos);
		if (doc.getRoot() == null) {
			throw new XMLParseException("No root element in XML document!");
		}
		doc.repack();
		return doc;
	}

	/**
	 * 试图解析一个元素及它的子元素
	 * 
	 * @param start 起始位置
	 * @return 结束位置
	 */
	private int expectElement(int start) {
		// parse tag's QName
		int index = expect(cs, start, "<");
		if (index < 0) {
			return -1;
		}
		start = index;
		for (int i = start; i < cs.length; i++) {
			char c = cs[i];
			if (isSpace(c) || c == '>') {
				index = i;
				break;
			}
		}
		tag = xml.substring(start, index);
		if (tag.charAt(tag.length() - 1) == '/') {
			tag = tag.substring(0, tag.length() - 1);
			index--;
		}
		if (index == start || isInvalidName(cs, start, index)) {
			throw new XMLParseException("Element's name is invalid:" + tag + ", line number is " + getLineNum(lineNumList, cs, start));
		}

		XMLElement ele = new XMLElement(tag);
		ele.setLineNumber(getLineNum(lineNumList, cs, start));
		ele.setStartCharIndex(start - 1);
		if (current == null) {
			doc.setRoot(ele);
		} else {
			ele.setParent(current);
		}
		current = ele;
		start = index;

		// parse attributes
		while (true) {
			ElementAttribute ea = expectAttribute(cs, start, lineNumList);
			if (ea == null) {
				break;
			}
			ele.addAttribute(ea.Name, ea.Value);
			start = ea.EndCharIndex;
		}
		if ((index = expect(cs, start, "/>")) > 0) {
			current = current.getParent();
			return index;
		}
		if ((index = expect(cs, start, ">")) < 0) {
			throw new XMLParseException("Element not complete correctly:" + tag + ", line number is " + getLineNum(lineNumList, cs, start));
		}

		// parse children
		while (true) {
			if (expect(cs, index, "</") > 0) {// exists text node only
				break;
			}
			int i = -1;
			i = expectOthers(index);
			if (i > 0) {
				index = i;
				continue;
			}
			i = expectText(index);
			if (i > 0) {
				current.addText(text);
				index = i;
				continue;
			}
			i = expectCDATA(index);
			if (i > 0) {
				current.addCDATA(cdata);
				index = i;
				continue;
			}
			i = expectElement(index);
			if (i > 0) {
				index = i;
				continue;
			}
			break;
		}
		start = index;

		// parse tag end
		index = expect(cs, start, "</" + ele.QName);
		if (index < 0) {
			throw new XMLParseException("Element not complete correctly:" + ele.QName + ", line number is "
					+ getLineNum(lineNumList, cs, start));
		} else {
			start = ignoreSpace(cs, index);
			index = expect(cs, start, ">");
			if (index < 0) {
				throw new XMLParseException("Element not complete correctly:" + ele.QName + ", line number is "
						+ getLineNum(lineNumList, cs, start));
			}
		}
		current.setEndCharIndex(index);
		current = current.getParent();
		return index;
	}

	/**
	 * 判断字符串是否是非法的标签名
	 */
	public static boolean isInvalidName(char[] cs, int start, int end) {
		char first = cs[start];
		if (isSpace(first)) {
			return true;
		}
		if (Character.isDigit(first)) {
			return true;
		}
		if (first == ':' || first == '.' || first == '-') {
			return true;
		}
		for (int i = start; i < end; i++) {
			if (isInvalidChar(cs[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param c 字符
	 * @return 是否是标签或属性名中的非法字符
	 */
	public static boolean isInvalidChar(char c) {
		switch (c) {
		case '!':
		case '@':
		case '#':
		case '~':
		case '`':
		case '%':
		case '$':
		case '^':
		case '*':
		case '(':
		case ')':
		case '+':
		case '=':
		case '{':
		case '}':
		case '[':
		case ']':
		case '|':
		case '\\':
		case '?':
		case '/':
		case '>':
		case '<':
		case '\"':
		case '\'':
			return true;
		default:
			return false;
		}
	}

	/**
	 * 试图解析标签中的文本
	 * 
	 * @param start 起始位置
	 * @return 结束位置
	 */
	private int expectText(int start) {
		int i = indexOf(cs, '<', start);
		if (i < 0) {
			throw new XMLParseException("Element not complete correctly:" + current.QName + ", line number is "
					+ getLineNum(lineNumList, cs, start));
		}
		if (i == start) {
			return -1;
		}
		String v = xml.substring(start, i).trim();
		if (v.length() == 0) {
			return -1;
		}
		text = decode(v);
		return i;
	}

	/**
	 * 试图解析一个CDATA块
	 * 
	 * @param start 起始位置
	 * @return 结束位置
	 */
	private int expectCDATA(int start) {
		int i = expect(cs, start, "<![CDATA[");
		if (i < 0) {
			return -1;
		}
		start = i;
		i = indexOf(cs, "]]>", start);
		if (i < 0) {
			throw new XMLParseException("CDATA not end correctly:" + current.QName + ", line number is "
					+ getLineNum(lineNumList, cs, start));
		}
		cdata = xml.substring(start, i);
		return i + 3;
	}

	/**
	 * 试图解析一个标签属性，形如：
	 * 
	 * <pre>
	 * a = &quot;b&quot;
	 * </pre>
	 */
	public static ElementAttribute expectAttribute(char[] cs, int start, List<Integer> lineNumList) {
		start = ignoreSpace(cs, start);
		if (cs[start] == '>' || cs[start] == '/' || cs[start] == '?') {
			return null;
		}
		String attrName = null;
		String attrValue = null;
		int index = -1;
		for (int i = start; i < cs.length; i++) {
			if (cs[i] == '=') {
				index = i;
				break;
			} else if (isSpace(cs[i])) {
				i = ignoreSpace(cs, i + 1);
				if (cs[i] == '=') {
					index = i;
				} else {
					return null;
				}
				break;
			}
		}
		String name = new String(cs, start, index - start).trim();
		if (isInvalidName(cs, start, start + name.length())) {
			throw new XMLParseException("XML attribute name is invalid: [" + name + "], line number is "
					+ getLineNum(lineNumList, cs, start));
		}
		start = index + 1;
		start = ignoreSpace(cs, start);
		char c = cs[start];
		if (c != '\"' && c != '\'') {
			throw new XMLParseException("XML attribute value not starts with \" or \', line number is "
					+ getLineNum(lineNumList, cs, start));
		}
		start++;
		for (int i = start; i < cs.length; i++) {
			if (cs[i] == c) {
				index = i;
				break;
			}
			if (cs[i] == '<') {
				throw new XMLParseException("XML attribute value can't contains <, line number is " + getLineNum(lineNumList, cs, start));
			}
		}
		String value = new String(cs, start, index - start);
		value = decode(value);
		attrName = name;
		attrValue = value;
		return new ElementAttribute(index + 1, attrName, attrValue);
	}

	/**
	 * 解码标签和属性中的实体字符。
	 */
	public static String decode(String value) {
		char[] buf = new char[value.length()];
		int j = 0;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '<') {
				throw new XMLParseException("XML attribute value is invalid: [" + value + "]");
			}
			if (c == '&') {
				if (value.startsWith("&quot;", i)) {
					buf[j++] = '\"';
					i += 5;
					continue;
				} else if (value.startsWith("&amp;", i)) {
					buf[j++] = '&';
					i += 4;
					continue;
				} else if (value.startsWith("&lt;", i)) {
					buf[j++] = '<';
					i += 3;
					continue;
				} else if (value.startsWith("&gt;", i)) {
					buf[j++] = '>';
					i += 3;
					continue;
				} else if (value.startsWith("&apos;", i)) {
					buf[j++] = '\'';
					i += 5;
					continue;
				} else if (value.charAt(i + 1) == '#') {
					int k = i + 2;
					boolean flag = false;
					int radix = 10;
					if (value.charAt(k) == 'x' || value.charAt(k) == 'X') {// 十六制进
						radix = 16;
						k++;
					}
					for (; k < i + 9 && k < value.length(); k++) {
						if (value.charAt(k) == ';') {
							flag = true;
							break;
						}
					}
					if (flag) {
						char ch = (char) Integer.parseInt(value.substring(radix == 10 ? i + 2 : i + 3, k), radix);
						buf[j++] = ch;
						i += k;
					} else {
						throw new XMLParseException("Attribute value or element body contains not supported entity: ["
								+ value.substring(i, i + 10 > value.length() ? value.length() : i + 10) + "]");
					}
				} else {
					throw new XMLParseException("Attribute value or element body contains not supported entity: ["
							+ value.substring(i, i + 10 > value.length() ? value.length() : i + 10) + "]");
				}
			} else {
				buf[j++] = c;
			}
		}
		return new String(buf, 0, j);
	}

	/**
	 * 尝试解析注释和处理指令。
	 */
	private int expectOthers(int start) {
		int end = -1;
		int old = start;

		while (true) {
			end = expect(cs, start, "<!--");// comment
			if (end < 0) {
				end = expect(cs, start, "<?");// processing instruction
				if (end < 0) {
					break;
				}
				start = end;
				end = indexOf(cs, "?>", start);
				if (end < 0) {
					throw new XMLParseException("Processing instruction not complete correctly, line number is "
							+ getLineNum(lineNumList, cs, start));
				}
				String instruction = xml.substring(start, end).trim();
				if (instruction.indexOf('?') >= 0) {
					throw new XMLParseException("Processing instruction contains illegal character:[?]");
				}
				if (instruction.indexOf('>') >= 0) {
					throw new XMLParseException("Processing instruction contains illegal character:[>]");
				}
				if (current == null) {
					doc.addInstruction(instruction);
				} else {
					current.addInstruction(instruction);
				}
				start = end + 2;
			} else {
				start = end;
				end = indexOf(cs, "--", start);
				if (end < 0) {
					throw new XMLParseException("Comment not complete correctly, line number is " + getLineNum(lineNumList, cs, start));
				}
				end = expect(cs, end + 2, ">");
				if (end < 0) {
					throw new XMLParseException("Comment not complete correctly,  line number is " + getLineNum(lineNumList, cs, start));
				}
				String comment = xml.substring(start, end - 3);
				if (current == null) {
					doc.addComment(comment);
				} else {
					current.addComment(comment);
				}
				start = end;
			}
		}
		if (old == start) {
			return -1;
		} else {
			return start;
		}
	}

	/**
	 * 获得从指定位置开始的第一个&gt的位置;
	 */
	private int expectGT(int start) {
		int index = indexOf(cs, '>', start);
		if (index < 0) {
			return -1;
		}
		for (int i = start; i < index; i++) {
			if (cs[i] == '<') {
				return -1;
			}
		}
		return index + 1;
	}

	public static int ignoreSpace(char[] cs, int start) {
		for (int i = start; i < cs.length; i++) {
			if (isSpace(cs[i])) {
				start++;
			} else {
				break;
			}
		}
		return start;
	}

	public static boolean isSpace(char c) {
		return c == ' ' || c == 160 || c == '\r' || c == '\n' || c == '\t' || c == '\b' || c == '\f';
	}

	/**
	 * 判断从指定字符串是否在指定的位置上开始匹配<br>
	 * 返回值表示匹配结束位置， 返回值为-1表示未匹配.
	 */
	public static int expect(char[] cs, int start, String expected) {
		start = ignoreSpace(cs, start);
		int end = start + expected.length();
		if (end > cs.length) {
			return -1;
		}
		for (int j = 0; j < expected.length(); j++) {
			int i = start + j;
			if (cs[i] != expected.charAt(j)) {
				return -1;
			}
		}
		return end;
	}

	/**
	 * 返回解析后的XMLDocument实例
	 */
	public XMLDocument getDocument() {
		return doc;
	}

	public static int indexOf(char[] cs, char c, int start) {
		for (int i = start; i < cs.length; i++) {
			if (cs[i] == c) {
				return i;
			}
		}
		return -1;
	}

	public static int indexOf(char[] cs, String str, int start) {
		boolean hasSame = false;
		int length = str.length();
		for (int i = start; i < cs.length - length + 1; i++) {
			hasSame = true;
			for (int j = 0; j < length; j++) {
				if (str.charAt(j) != cs[i + j]) {
					hasSame = false;
					break;
				}
			}
			if (hasSame) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获取指定位置处于第几行
	 */
	public static int getLineNum(List<Integer> lineNumList, char[] cs, int charIndex) {
		if (lineNumList.size() == 0) {
			for (int i = 0; i < cs.length; i++) {
				if (cs[i] == '\n') {
					lineNumList.add(i);
				}
			}
		}
		int start = new Double(charIndex * 1.0 / cs.length * lineNumList.size()).intValue();
		if (lineNumList.size() > start && lineNumList.get(start) > charIndex) {
			for (int i = start - 1; i >= 0; i--) {
				if (lineNumList.get(i) < charIndex) {
					return i + 2;// 必须加2
				}
			}
			return 1;
		} else {
			for (int i = start + 1; i < lineNumList.size(); i++) {
				if (lineNumList.get(i) > charIndex) {
					return i + 1;// 只需加1
				}
			}
			return lineNumList.size() + 1;
		}
	}

	public static class ElementAttribute {
		public int EndCharIndex;
		public String Name;
		public String Value;

		/**
		 * @param index 起始位置
		 * @param name 属性名
		 * @param value 属性值
		 */
		public ElementAttribute(int index, String name, String value) {
			EndCharIndex = index;
			Name = name;
			Value = value;
		}
	}
}
