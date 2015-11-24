package com.zving.framework.utility;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.collection.Mapx;

/**
 * ${A}格式的简易正则表达式解析器，避免了常规正则表达式需要转义太多字符的毛病<br>
 * <br>
 * 在简易表达式中，<font color='red'>${T:Name}</font>表示一个占位符，其中:<br>
 * <font color='red'> T </font>表示占位符类型,<font color='red'> Name
 * </font>表示占位符的引用名称，解释之后得到一组Mapx，在这些Mapx中可以通过Name引用匹配到的值。
 * T共有A,D,W,-D,-W五种类型，共中A表示.*?，D表示\d*?，W表示任意长度的\w*?,-D表示\D*?,-W表示\W*?<br>
 * 别外还可以使用<font color='red'> ${{Pattern:Name}} </font>的方式直接使用正常的正则表达式 <br>
 * <br>
 * 注意：本类线程不安全。
 * 
 * @Author 王育春
 * @Date 2007-9-6
 * @Mail wyuch@zving.com
 */
public class RegexParser {
	String regex;

	int currentPos;

	String orginalText;

	ArrayList<Object> list = new ArrayList<Object>(16);

	ArrayList<String> groups = null;// 本次匹配结果,包括未命名的结果

	Mapx<String, int[]> map = null;// 本次匹配的名值映射

	RegexMatch rm = null;// 本次匹配的结果

	int startPos = 0;

	boolean caseIngore = true;

	char[] cs = null;

	public RegexParser(String regex) {
		this(regex, true);
	}

	/**
	 * caseIngoreFlag表示匹配时不区分大小写。
	 */
	public RegexParser(String regex, boolean caseIngoreFlag) {
		regex = regex.replaceAll("\\s*\\n\\s*", "\n").replaceAll("\\s*(\\$\\{A.*?\\})\\s*", "$1").trim();
		this.regex = caseIngoreFlag ? regex.toLowerCase() : regex;
		caseIngore = caseIngoreFlag;
		parse();
	}

	public String getText() {
		return orginalText;
	}

	public void setText(String text) {
		orginalText = text;
		cs = text.toCharArray();
		if (caseIngore) {
			for (int i = 0; i < cs.length; i++) {
				char c = cs[i];
				if (c >= 65 && c <= 90) {
					c += 32;
					cs[i] = c;
				}
			}
		}
		currentPos = 0;// 重新开始匹配
		startPos = 0;
	}

	private void parse() {
		if (StringUtil.isEmpty(regex)) {
			throw new RegexMatchFailedException("Simple regex expression can't be emtpy");
		}
		int lastIndex = 0;
		while (true) {
			int start = regex.indexOf("${", lastIndex);
			if (start < 0) {
				break;
			}
			int end = regex.indexOf("}", start);
			if (end < 0) {
				break;
			}
			String previous = regex.substring(lastIndex, start);
			if (StringUtil.isNotEmpty(previous)) {
				list.add(previous);
			}
			String item = regex.substring(start + 2, end);
			list.add(new RegexItem(item));
			lastIndex = end + 1;
		}
		if (lastIndex != regex.length()) {
			String str = regex.substring(lastIndex);
			list.add(str);
		}
	}

	public boolean match() {
		groups = new ArrayList<String>();
		map = new Mapx<String, int[]>();
		RegexMatch rm = new RegexMatch(this, 0, currentPos);
		try {
			if (rm.match()) {
				currentPos = rm.getEnd();
				startPos = rm.getStart();
				return true;
			}
		} catch (RegexMatchFailedException e) {
			return false;
		}
		return false;
	}

	/**
	 * 本次匹配开始位置
	 */
	public int getMatchStart() {
		return startPos;
	}

	/**
	 * 本次匹配结束位置
	 */
	public int getMatchEnd() {
		return currentPos;
	}

	public String replace(String content, String replacement) {
		this.setText(content);
		StringBuilder sb = new StringBuilder();
		int lastIndex = 0;
		while (this.match()) {
			sb.append(this.orginalText.substring(lastIndex, startPos));
			sb.append(replacement);
			lastIndex = currentPos;
		}
		if (lastIndex < this.orginalText.length()) {
			sb.append(this.orginalText.substring(lastIndex));
		}
		return sb.toString();
	}

	public String[] getGroups() {
		if (groups == null) {
			return null;
		}
		String[] arr = null;
		return groups.toArray(arr);
	}

	public Mapx<String, String> getMapx() {
		if (map == null) {
			return null;
		}
		CaseIgnoreMapx<String, String> r = new CaseIgnoreMapx<String, String>();
		for (Entry<String, int[]> e : map.entrySet()) {
			int[] arr = e.getValue();
			String v = orginalText.substring(arr[0], arr[1]);
			r.put(e.getKey(), v);
		}
		return r;
	}

	public String getMacthedText() {
		return this.orginalText.substring(startPos, currentPos);
	}
}
