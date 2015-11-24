package com.zving.framework.template;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import com.zving.framework.thirdparty.fastjson.IOUtils;

/**
 * 模板输出，能够通过getResult()得到写入的字符串。
 * 
 * @Author 王育春
 * @Date 2010-7-26
 * @Mail wyuch@zving.com
 */
public class TemplateWriter extends PrintWriter {
	private static ThreadLocal<SoftReference<char[]>> local = new ThreadLocal<SoftReference<char[]>>();

	PrintWriter out;
	char[] values;
	int mainPos;
	int flushPos = 0;

	List<Integer> bufferPosList = new ArrayList<Integer>();
	int bufferStart = -1;
	int bufferEnd;

	public TemplateWriter() {
		super(new StringWriter());
		if (local == null) {
			local = new ThreadLocal<SoftReference<char[]>>();
		}
		SoftReference<char[]> sr = local.get();
		if (sr != null) {
			values = sr.get();
			local.set(null);
		}
		if (values == null) {
			values = new char[102400];
		}
	}

	public TemplateWriter(PrintWriter out) {
		this();
		this.out = out;
	}

	public void beginBuffer() {
		if (bufferStart == -1) {// 已经有缓冲了
			bufferPosList.add(mainPos);
			bufferStart = bufferEnd = mainPos;
		} else {
			bufferPosList.add(bufferEnd);
			bufferStart = bufferEnd;
		}
	}

	public void commitBuffer() {
		if (bufferStart == -1) {
			return;
		}
		bufferPosList.remove(bufferPosList.size() - 1);// 移掉最后一个
		if (bufferPosList.size() > 0) {
			bufferStart = bufferPosList.get(bufferPosList.size() - 1);// 重置开始，结束不需要重置
		} else {
			bufferStart = -1;
			mainPos = bufferEnd;
			tryFlush();
		}
	}

	public void clearBuffer() {
		if (bufferStart == -1) {
			return;
		}
		bufferEnd = bufferStart;
	}

	public String getBuffer() {
		if (bufferStart != -1) {
			return new String(values, bufferStart, bufferEnd - bufferStart);
		} else {
			return "";
		}
	}

	public int length() {// NO_UCD
		return mainPos;
	}

	void expandCapacity(int newCount) {
		int newCapacity = (values.length + 1) * 2;
		if (newCapacity < 0) {
			newCapacity = Integer.MAX_VALUE;
		} else if (newCount > newCapacity) {
			newCapacity = newCount;
		}
		char newValues[] = new char[newCapacity];
		System.arraycopy(values, 0, newValues, 0, bufferStart != -1 ? bufferEnd : mainPos);
		values = newValues;
	}

	public void write(char c) {
		int pos = bufferStart != -1 ? bufferEnd : mainPos;
		int newCount = pos + 1;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		values[pos] = c;
		if (bufferStart != -1) {
			bufferEnd = newCount;
		} else {
			mainPos = newCount;
			tryFlush();
		}
	}

	@Override
	public void write(char[] cs) {
		write(cs, 0, cs.length);
	}

	@Override
	public void write(char[] cs, int offset, int length) {
		int pos = bufferStart != -1 ? bufferEnd : mainPos;
		int newCount = pos + length;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		System.arraycopy(cs, offset, values, pos, length);
		if (bufferStart != -1) {
			bufferEnd = newCount;
		} else {
			mainPos = newCount;
			tryFlush();
		}
	}

	@Override
	public void write(String s, int offset, int length) {
		int pos = bufferStart != -1 ? bufferEnd : mainPos;
		int newCount = pos + length;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		s.getChars(offset, offset + length, values, pos);
		if (bufferStart != -1) {
			bufferEnd = newCount;
		} else {
			mainPos = newCount;
			tryFlush();
		}
	}

	@Override
	public void write(int i) {
		if (i == Integer.MIN_VALUE) {
			write("-2147483648");
			return;
		}
		int pos = bufferStart != -1 ? bufferEnd : mainPos;
		int size = i < 0 ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
		int newCount = pos + size;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		IOUtils.getChars(i, newCount, values);
		if (bufferStart != -1) {
			bufferEnd = newCount;
		} else {
			mainPos = newCount;
			tryFlush();
		}
	}

	public void write(long i) {
		if (i == Long.MIN_VALUE) {
			write("-9223372036854775808");
			return;
		}
		int pos = bufferStart != -1 ? bufferEnd : mainPos;
		int size = i < 0 ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
		int newCount = pos + size;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		IOUtils.getChars(i, newCount, values);
		if (bufferStart != -1) {
			bufferEnd = newCount;
		} else {
			mainPos = newCount;
			tryFlush();
		}
		tryFlush();
	}

	/**
	 * 请使用write()替代
	 */
	@Override
	@Deprecated
	public void print(String str) {
		write(str);
	}

	/**
	 * 请使用write()替代
	 */
	@Override
	@Deprecated
	public void print(Object obj) {
		write(obj);
	}

	/**
	 * 请使用write(str+"\n")替代
	 */
	@Override
	@Deprecated
	public void println(String str) {
		newLine();
		write(str);
	}

	@Override
	public void write(String str) {
		if (str == null) {
			write("null");
			return;
		}
		int pos = bufferStart != -1 ? bufferEnd : mainPos;
		int length = str.length();
		int newCount = pos + length;
		if (newCount > values.length) {
			expandCapacity(newCount);
		}
		str.getChars(0, length, values, pos);
		if (bufferStart != -1) {
			bufferEnd = newCount;
		} else {
			mainPos = newCount;
			tryFlush();
		}
		tryFlush();
	}

	public void write(Object obj) {
		if (obj == null) {
			write("null");
			return;
		}
		write(String.valueOf(obj));
	}

	@Override
	public String toString() {
		return new String(values, 0, mainPos);
	}

	public String getResult() {
		String v = toString();
		close();
		return v;
	}

	void tryFlush() {
		if (out != null) {
			if (mainPos - flushPos > 8196) {
				flush();
			}
		}
	}

	@Override
	public void flush() {
		if (out != null && mainPos > flushPos) {
			out.write(values, flushPos, mainPos - flushPos);
			flushPos = mainPos;
		}
	}

	@Override
	public void close() {
		mainPos = 0;
		bufferPosList.clear();
		bufferStart = bufferEnd = 0;
		local.set(new SoftReference<char[]>(values));
	}

	private void newLine() {
		write('\n');
	}

}
