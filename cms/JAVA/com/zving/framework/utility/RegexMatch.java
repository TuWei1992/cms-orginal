package com.zving.framework.utility;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-12-15
 */
public class RegexMatch {

	/**
	 * 表达式的第几个部分
	 */
	private int no;

	/**
	 * 文本中的当前位置
	 */
	private int currentPos;

	/**
	 * 本次匹配在文本中的起始位置
	 */
	private int startPos;

	private int end;

	private RegexParser rp = null;

	public RegexMatch(RegexParser rp, int no, int start) {
		this.rp = rp;
		this.currentPos = start;
		this.no = no;
	}

	public void setStart(int start) {
		this.currentPos = start;
	}

	private int[] indexOf(char[] cs, String item, int pos) {
		int r = pos, i = pos, j = 0;
		boolean flag = false;
		int s1 = cs.length;
		int s2 = item.length();
		for (; i < s1 && j < s2; i++, j++) {
			char c1 = cs[i];
			char c2 = item.charAt(j);
			if (j == 0) {
				r = i;
			}
			if (c2 == ' ' || c2 == '\n' || c2 == '\t' || c2 == '\r' || c2 == '\b' || c2 == '\f' || c2 == '　') {
				int tmp = 0;
				while (c1 == ' ' || c1 == '\n' || c1 == '\t' || c1 == '\r' || c1 == '\b' || c1 == '\f' || c1 == '　') {
					tmp++;
					c1 = cs[i + tmp];
				}
				i = i + tmp - 1;
			} else if (c1 != c2) {
				j = -1;
			}
			if (j == s2 - 1) {
				flag = true;
			}
		}
		if (flag) {
			return new int[] { r, i };
		} else {
			return new int[] { -1, -1 };
		}
	}

	public boolean match() {
		if (no >= rp.list.size()) {
			return false;
		}
		Object obj = rp.list.get(no);
		boolean matchFlag = false;
		RegexMatch rm = null;
		if (obj instanceof String) {
			String item = obj.toString();
			while (true) {
				int[] arr = indexOf(rp.cs, item, currentPos);
				int pos = arr[0];
				int itemEndPos = arr[1];
				if (pos < currentPos) {
					throw new RegexMatchFailedException(item);
				}
				if (no != 0 && pos != currentPos) {
					return false;
				}
				startPos = pos;
				if (no == rp.list.size() - 1) {
					end = itemEndPos;
					return true;
				}
				rm = new RegexMatch(rp, no + 1, itemEndPos);
				if (rm.match()) {
					matchFlag = true;
					break;
				} else {
					if (no == 0) {
						currentPos = pos + 1;// 如果是第一个字符串，则可以继续找下一个匹配
					} else {
						return false;
					}
				}
			}
		} else {
			RegexItem ri = (RegexItem) rp.list.get(no);
			String v = null;
			int nextPos = currentPos;
			if (no == rp.list.size() - 1) {
				nextPos = currentPos + 1;
				while (true) {
					if (ri.match(rp.cs, currentPos, nextPos - currentPos) < 0) {
						break;
					}
					nextPos++;
				}
				nextPos--;
				v = rp.orginalText.substring(currentPos, nextPos);
				rp.groups.add(v);
				if (ri.getName() != null) {
					rp.map.put(ri.getName(), new int[] { currentPos, nextPos });
				}
				return true;
			}
			while (true) {
				int[] arr = indexOf(rp.cs, rp.list.get(no + 1).toString(), nextPos);
				nextPos = arr[0];
				if (nextPos < 0) {
					throw new RegexMatchFailedException(rp.list.get(no + 1).toString());
				}
				if (ri.match(rp.cs, currentPos, nextPos - currentPos) < 0) {
					return false;
				}
				rm = new RegexMatch(rp, no + 1, nextPos);
				if (rm.match()) {
					matchFlag = true;
					v = rp.orginalText.substring(currentPos, nextPos);
					rp.groups.add(v);
					if (ri.getName() != null) {
						rp.map.put(ri.getName(), new int[] { currentPos, nextPos });
					}
					break;
				} else {
					nextPos++;
				}
			}
		}
		if (matchFlag) {
			end = rm.getEnd();
		}
		return true;
	}

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return startPos;
	}
}
