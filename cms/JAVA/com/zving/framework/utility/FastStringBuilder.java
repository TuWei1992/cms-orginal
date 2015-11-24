package com.zving.framework.utility;

import java.lang.ref.SoftReference;

import com.zving.framework.thirdparty.fastjson.IOUtils;

/**
 * 快速的字符串构造器，在构造长度较长(100个字符以上)的字符串时有性能优势，在非框架代码中需要谨慎使用。<br>
 * <br>
 * 注意： 使用后需要调用close()方法，或者使用toStringAndClose()。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-11
 */
public class FastStringBuilder {
	private static ThreadLocal<SoftReference<char[]>> local = new ThreadLocal<SoftReference<char[]>>();
	private char[] values;
	private int count;

	public FastStringBuilder() {
		if (local == null) {
			local = new ThreadLocal<SoftReference<char[]>>();
		}
		SoftReference<char[]> sr = local.get();
		if (sr != null) {
			values = sr.get();
			local.set(null);
		}
		if (values == null) {
			values = new char[5120];
		}
	}

	public int length() {// NO_UCD
		return count;
	}

	void expandCapacity(int newCount) {
		int newCapacity = values.length * 2;
		if (newCapacity < newCount) {
			newCapacity = newCount;
		}
		char newValues[] = new char[newCapacity];
		System.arraycopy(values, 0, newValues, 0, count);
		values = newValues;
	}

	public FastStringBuilder append(char c) {
		int newCount = count + 1;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		values[count] = c;
		count = newCount;
		return this;
	}

	public FastStringBuilder append(char[] cs) {// NO_UCD
		int newCount = count + cs.length;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		System.arraycopy(cs, 0, values, count, cs.length);
		count = newCount;
		return this;
	}

	public FastStringBuilder append(char[] cs, int offset, int length) {// NO_UCD
		int newCount = count + length;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		System.arraycopy(cs, offset, values, count, length);
		count = newCount;
		return this;
	}

	public FastStringBuilder append(String s, int offset, int length) {
		int newCount = count + length;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		s.getChars(offset, offset + length, values, count);
		count = newCount;
		return this;
	}

	public FastStringBuilder append(int i) {
		if (i == Integer.MIN_VALUE) {
			return append("-2147483648");
		}
		int size = i < 0 ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
		int newCount = count + size;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		IOUtils.getChars(i, newCount, values);
		count = newCount;
		return this;
	}

	public FastStringBuilder append(long i) {
		if (i == Long.MIN_VALUE) {
			return append("-9223372036854775808");
		}
		int size = i < 0 ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
		int newCount = count + size;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		IOUtils.getChars(i, newCount, values);
		count = newCount;
		return this;
	}

	public FastStringBuilder append(String str) {
		if (str == null) {
			return append("null");
		}
		int length = str.length();
		int newCount = count + length;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		str.getChars(0, length, values, count);
		count = newCount;
		return this;
	}

	public FastStringBuilder append(Object obj) {
		if (obj == null) {
			return append("null");
		}
		if (obj instanceof Long) {
			return append(((Long) obj).longValue());
		}
		if (obj instanceof Integer) {
			return append(((Integer) obj).intValue());
		}
		if (obj instanceof Character) {
			return append(((Character) obj).charValue());
		}
		return append(String.valueOf(obj));
	}

	public void clear() {
		count = 0;
	}

	public String toStringAndClose() {
		String v = toString();
		close();
		return v;
	}

	@Override
	public String toString() {
		return new String(values, 0, count);
	}

	public void close() {
		local.set(new SoftReference<char[]>(values));
	}

}
