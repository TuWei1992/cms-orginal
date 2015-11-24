package com.zving.framework.core.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.core.IURLHandler;

/**
 * Html处理者虚拟类，输出html的处理者可以继承本类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-20
 */
public abstract class AbstractHtmlHandler implements IURLHandler {

	@Override
	public boolean handle(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setHeader("Pragma", "No-Cache");
		response.setHeader("Cache-Control", "No-Cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("text/html");
		return execute(url, request, response);
	}

	public abstract boolean execute(String url, HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException;

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}
}
