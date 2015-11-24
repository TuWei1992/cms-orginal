package com.zving.framework.template;

import com.zving.framework.collection.Mapx;

/**
 * 表示模板中的一个片段
 * 
 * @Author 王育春
 * @Date 2010-5-29
 * @Mail wyuch@zving.com
 */
public class TemplateFragment {
	/**
	 * 普通HTML
	 */
	public static final int FRAGMENT_HTML = 1;

	/**
	 * 标签
	 */
	public static final int FRAGMENT_TAG = 2;

	/**
	 * 表达式
	 */
	public static final int FRAGMENT_EXPRESSION = 3;

	/**
	 * 脚本
	 */
	public static final int FRAGMENT_SCRIPT = 4;

	/**
	 * 注释
	 */
	public static final int FRAGMENT_COMMENT = 5;

	public int Type;

	public String TagPrefix;// 只有类型为FRAGMENT_TAG有值

	public String TagName;// 只有类型为FRAGMENT_TAG有值

	public Mapx<String, String> Attributes;// 只有类型为FRAGMENT_TAG有值

	public String FragmentText;// 片段中的文本，指不包括起始符（标签本身）的部分

	public String TagSource;// 标签源代码

	public int StartLineNo;

	public int StartCharIndex;// 标签开始位置

	public int EndCharIndex;// 标签 结束位置

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (Type == FRAGMENT_HTML) {
			sb.append("HTML");
		} else if (Type == FRAGMENT_TAG) {
			sb.append("TAG");
		} else if (Type == FRAGMENT_SCRIPT) {
			sb.append("SCRIPT");
		} else if (Type == FRAGMENT_EXPRESSION) {
			sb.append("EXPRESSION");
		} else if (Type == FRAGMENT_COMMENT) {
			sb.append("COMMENT");
		}
		sb.append(":");
		if (Type == FRAGMENT_TAG) {
			sb.append("<" + TagPrefix + ":" + TagName);
		}
		if (FragmentText != null) {
			String str = FragmentText.replaceAll("[\\n\\r]+", "\\\\n");
			str = FragmentText.replaceAll("\\s+", " ");
			if (str.length() > 100) {
				str = str.substring(0, 100);
			}
			sb.append(str);
		}
		return sb.toString();
	}
}
