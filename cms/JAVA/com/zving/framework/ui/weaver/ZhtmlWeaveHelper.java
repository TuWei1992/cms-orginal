package com.zving.framework.ui.weaver;

import com.zving.framework.utility.RegexParser;

/**
 * Zhtml织入助手
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-10-17
 */
public class ZhtmlWeaveHelper {
	String source;
	String result;

	/**
	 * 构造器
	 */
	public ZhtmlWeaveHelper(String source) {
		result = this.source = source;
	}

	/**
	 * 包含一个文件到指定位置
	 */
	public void includeFile(ZhtmlWeavePosition pos, String fileName) {
		insertCode(pos, "\n<%@include file=\"" + fileName + "\"%>\n");
	}

	/**
	 * 包含一个文件到源代码中符合指定简易正则表达式的字符串之后
	 */
	public void includeFileAfter(RegexParser re, String fileName) {
		insertCodeAfter(re, "\n<%@include file=\"" + fileName + "\"%>\n");
	}

	/**
	 * 包含一个文件到源代码中符合指定简易正则表达式的字符串之前
	 */
	public void includeFileBefore(RegexParser re, String fileName) {
		insertCodeBefore(re, "\n<%@include file=\"" + fileName + "\"%>\n");
	}

	/**
	 * 包含一个文件到源代码的指定行,行号以1开始
	 */
	public void includeFile(int lineNumber, String fileName) {
		insertCode(lineNumber, "\n<%@include file=\"" + fileName + "\"%>\n");
	}

	/**
	 * 插入代码到指定位置
	 */
	public void insertCode(ZhtmlWeavePosition pos, String code) {
		if (pos == ZhtmlWeavePosition.AfterDocumentEnd) {
			result = source + code;
		} else if (pos == ZhtmlWeavePosition.BeforeDocumentStart) {
			result = code + source;
		} else if (pos == ZhtmlWeavePosition.AfterBodyStart) {
			insertCodeAfter(ZhtmlWeavePosition.Regex_BodyStart, code);
		} else if (pos == ZhtmlWeavePosition.AfterHeadStart) {
			insertCodeAfter(ZhtmlWeavePosition.Regex_HeadStart, code);
		} else if (pos == ZhtmlWeavePosition.BeforeBodyEnd) {
			insertCodeBefore(ZhtmlWeavePosition.Regex_BodyEnd, code);
		} else if (pos == ZhtmlWeavePosition.BeforeHeadEnd) {
			insertCodeBefore(ZhtmlWeavePosition.Regex_HeadEnd, code);
		} else if (pos == ZhtmlWeavePosition.BeforeHtmlEnd) {
			insertCodeBefore(ZhtmlWeavePosition.Regex_HtmlEnd, code);
		}
	}

	/**
	 * 插入代码到源代码中符合指定简易正则表达式的字符串之后
	 */
	public void insertCodeAfter(RegexParser re, String code) {
		re.setText(result);
		if (re.match()) {
			insertAt(re.getMatchEnd(), code);
		}
	}

	/**
	 * 插入代码到源代码中符合指定简易正则表达式的字符串之前
	 */
	public void insertCodeBefore(RegexParser re, String code) {
		re.setText(result);
		if (re.match()) {
			insertAt(re.getMatchStart(), code);
		}
	}

	/**
	 * 插入代码到源代码的指定行
	 */
	public void insertCode(int lineNumber, String code) {
		int last = 0;
		int i = 1;
		int pos = 0;
		while (true) {
			if (lineNumber == i) {
				pos = last;
				break;
			}
			int index = result.indexOf('\n', last);
			if (index < 0) {
				return;
			}
			last = index + 1;
		}
		insertAt(pos, code);
	}

	private void insertAt(int pos, String code) {
		StringBuilder sb = new StringBuilder(result.length() + code.length());
		sb.append(result.substring(0, pos));
		sb.append(code);
		sb.append(result.substring(pos));
		result = sb.toString();
	}

	/**
	 * 按行号删除代码，行号以1开始。
	 */
	public void deleteCode(int startLineNumber, int endLineNumber) {
		int last = 0;
		int i = 1;
		int startPos = 0;
		int endPos = 0;
		while (true) {
			if (startLineNumber == i) {
				startPos = last;
			}
			if (endLineNumber == i) {
				endPos = last;
				break;
			}
			int index = result.indexOf('\n', last);
			if (index < 0) {
				break;
			}
			last = index + 1;
			i++;
		}
		StringBuilder sb = new StringBuilder(result.length() - endPos + startPos);
		sb.append(result.substring(0, startPos));
		sb.append(result.substring(endPos));
		result = sb.toString();
	}

	/**
	 * 按简易正则表达式删除代码
	 */
	public void deleteCode(RegexParser reStart, RegexParser reEnd) {
		reStart.setText(result);
		if (!reStart.match()) {
			return;
		}
		int startPos = reStart.getMatchStart();
		reEnd.setText(result);
		if (!reEnd.match()) {
			return;
		}
		int endPos = reEnd.getMatchEnd();
		StringBuilder sb = new StringBuilder(result.length() - endPos + startPos);
		sb.append(result.substring(0, startPos));
		sb.append(result.substring(endPos));
		result = sb.toString();
	}

	/**
	 * 返回原始代码
	 */
	public String getSource() {
		return source;
	}

	/**
	 * 返回织入结果
	 */
	public String getResult() {
		return result;
	}
}
