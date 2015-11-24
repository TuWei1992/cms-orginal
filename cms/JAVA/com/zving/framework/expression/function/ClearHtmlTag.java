package com.zving.framework.expression.function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zving.framework.expression.AbstractFunction;
import com.zving.framework.expression.IVariableResolver;

/**
 * 清除字符串中的HTMl标签
 * 
 * @author 王朝辉
 * @mail alex.wang@zving.com
 * @date 2012-11-22
 */
public class ClearHtmlTag extends AbstractFunction {
	public static String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
	public static String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
	public static String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

	@Override
	public String getFunctionName() {
		return "clearHtmlTag";
	}

	private static String clearHtml(String htmlStr) {
		Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll(""); // 过滤script标签

		Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
		Matcher m_style = p_style.matcher(htmlStr);
		htmlStr = m_style.replaceAll(""); // 过滤style标签

		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll(""); // 过滤html标签

		htmlStr = htmlStr.replaceAll("[\\s]{2,}", " ");
		htmlStr = htmlStr.trim();
		htmlStr = htmlStr.replaceAll("&nbsp;", " ").replaceAll("&quot;", "\"").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
				.replaceAll("&amp;", "&");

		return htmlStr;
	}

	@Override
	public Object execute(IVariableResolver resolver, Object... args) {
		String htmlStr = (String) args[0];
		if (htmlStr == null) {
			return "";
		}
		return clearHtml(htmlStr);
	}

	@Override
	public String getFunctionPrefix() {
		return "";
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return AbstractFunction.Arg_String;
	}

}
