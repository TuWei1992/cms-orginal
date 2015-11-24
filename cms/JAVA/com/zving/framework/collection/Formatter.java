package com.zving.framework.collection;

import java.lang.reflect.Array;

import com.zving.framework.utility.FastStringBuilder;

/**
 * 按自定义格式逐项输出字符串
 * 
 * @Author 王育春
 * @Date 2008-4-16
 * @Mail wyuch@zving.com
 */
public abstract class Formatter {
	/**
	 * 格式化对象，返回字符串
	 */
	public abstract String format(Object obj);

	/**
	 * 默认格式化类，会输出数组元素的内容
	 */
	public static Formatter DefaultFormatter = new Formatter() {
		@Override
		public String format(Object o) {
			if (o == null) {
				return null;
			}
			if (o.getClass().isArray()) {
				FastStringBuilder sb = new FastStringBuilder();
				sb.append("{");
				for (int i = 0; i < Array.getLength(o); i++) {
					if (i != 0) {
						sb.append(",");
					}
					sb.append(Array.get(o, i));
				}
				sb.append("}");
				return sb.toStringAndClose();
			}
			return o.toString();
		}
	};
}
