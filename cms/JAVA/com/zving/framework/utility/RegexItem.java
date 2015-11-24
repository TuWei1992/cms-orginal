package com.zving.framework.utility;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-12-15
 */
public class RegexItem {
	private String name;

	private String expr;

	private char[] charArr;

	private boolean exclusive;// 排除模式

	private boolean greaterFlag = true;

	private int needCount = 0;

	public String toString() {
		return "${" + expr + "}";
	}

	public RegexItem(String item) {
		int index = item.lastIndexOf(':');
		if (index >= 0) {// 有可能没有名称
			name = item.substring(index + 1);
			expr = item.substring(0, index);
		} else {
			expr = item;
		}
		if (StringUtil.isEmpty(expr)) {
			throw new RegexMatchFailedException("Invalid simple regex item:" + item);
		}
		// 先将五种简易写法翻译成一般写法
		if (expr.equalsIgnoreCase("A")) {
			expr = "-[]*";
		} else if (expr.equalsIgnoreCase("D")) {
			expr = "[\\d]*";
		} else if (expr.equalsIgnoreCase("-D")) {
			expr = "-[\\d]*";
		} else if (expr.equalsIgnoreCase("W")) {
			expr = "[\\w]*";
		} else if (expr.equalsIgnoreCase("-W")) {
			expr = "-[\\w]*";
		}
		if (expr.startsWith("-")) {
			exclusive = true;
			expr = expr.substring(1);
		}
		int end = expr.lastIndexOf(']');
		int start = expr.indexOf('[');
		if (start != 0) {// 表明是表达式
			throw new RegexMatchFailedException("Invalid simple regex item:" + expr);
		}
		if (start < 0 || end < 0 || end < start) {
			throw new RegexMatchFailedException("Invalid simple regex item:" + expr);
		}
		String content = expr.substring(start + 1, end);
		if (end < expr.length() - 1) {
			// 以下解析匹配次数
			String tail = expr.substring(end + 1);
			if (tail.length()==0) {
				greaterFlag = false;
				needCount = 1;
			} else if (tail.equals("*")) {
				greaterFlag = true;
				needCount = 0;
			} else if (tail.equals("+")) {
				greaterFlag = true;
				needCount = 1;
			} else if (tail.endsWith("+")) {// 形如3+,表示3次以上
				greaterFlag = true;
				try {
					needCount = Integer.parseInt(tail.substring(0, tail.length() - 1));
				} catch (Exception e) {
					throw new RegexMatchFailedException("Invalid simple regex item:" + expr);
				}
			} else {// 指定次数
				greaterFlag = false;
				try {
					needCount = Integer.parseInt(tail.substring(0, tail.length()));
				} catch (Exception e) {
					throw new RegexMatchFailedException("Invalid simple regex item:" + expr);
				}
			}
		}
		FastStringBuilder sb = new FastStringBuilder();
		boolean flag = false;// 转义标志
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (flag) {
				if (c == 'd') {
					sb.append("0123456789");
				} else if (c == 'w') {
					sb.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789-");
				} else if (c == 's') {
					sb.append("\t\n\b\f\r");
				} else if (c == 't') {
					sb.append("\t");
				} else if (c == 'n') {
					sb.append("\n");
				} else if (c == 'b') {
					sb.append("\n");
				} else if (c == 'f') {
					sb.append("\f");
				} else if (c == 'r') {
					sb.append("\r");
				} else if (c == '\\') {
					sb.append("\\");
				} else {
					sb.append(c);// 除上述以外，转义符无作用
				}
			} else if (c != '\\') {
				sb.append(c);
			}
			if (c == '\\') {
				flag = true;
			} else {
				flag = false;
			}
		}
		charArr = sb.toStringAndClose().toCharArray();
	}

	public int match(char[] cs, int startPos, int extraCount) {
		int pos = startPos;
		int count = needCount + extraCount;
		for (int j = 0; j < count; j++) {
			if (pos >= cs.length) {
				return -1;
			}
			char c = cs[pos];
			boolean flag = false;
			boolean endSpaceFlag = false;
			for (int i = 0; i < charArr.length; i++) {
				if (charArr[i] == c) {
					if (this.exclusive) {// 说明匹配失败
						return -1;
					}
					flag = true;
					break;
				}
			}
			if (endSpaceFlag && c != ' ') {
				return -1;
			}
			if (!exclusive && !flag) {
				if (c != ' ') {
					return -1;
				} else {
					endSpaceFlag = true;
				}
			}
			pos++;
		}
		return pos;
	}

	public String getName() {
		return name;
	}

	public boolean isGreaterFlag() {
		return greaterFlag;
	}
}
