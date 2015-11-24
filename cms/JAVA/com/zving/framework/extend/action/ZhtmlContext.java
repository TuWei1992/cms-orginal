package com.zving.framework.extend.action;

import java.util.ArrayList;

import com.zving.framework.RequestData;

/**
 * Zhtml扩展点执行上下文
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-8-11
 */
public class ZhtmlContext {
	StringBuilder sb = new StringBuilder();
	ArrayList<String> includes = new ArrayList<String>();
	RequestData request = null;

	public ZhtmlContext(RequestData request) {
		this.request = request;
	}

	public RequestData getRequest() {
		return request;
	}

	protected String getOut() {
		return sb.toString();
	}

	protected ArrayList<String> getIncludes() {
		return includes;
	}

	/**
	 * 输出字符中到页面
	 */
	public void write(Object obj) {
		sb.append(obj);
	}

	/**
	 * 包含一个页面到当前页面
	 */
	public void include(String file) {
		includes.add(file);
	}
}
