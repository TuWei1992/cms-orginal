package com.zving.framework.core.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.CookieData;
import com.zving.framework.Current;
import com.zving.framework.core.IURLHandler;
import com.zving.framework.core.exception.UIMethodNotFoundException;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.data.BlockingTransaction;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.security.VerifyCheck;
import com.zving.framework.utility.LogUtil;

/**
 * ZAction处理者
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-5
 */
public class ActionHandler implements IURLHandler {
	public static final String ID = "com.zving.framework.core.ActionHandler";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "ZAction URL Processor";
	}

	@Override
	public boolean match(String url) {
		int i = url.indexOf('?');
		if (i > 0) {
			url = url.substring(0, i);
		}
		if (url.endsWith(".zaction")) {
			return true;
		}
		if (url.endsWith("/")) {
			return false;
		}
		i = url.lastIndexOf('/');
		if (i > 0) {
			url = url.substring(i + 1);
		}
		if (url.indexOf('.') < 0) {// 无后缀也匹配
			return true;
		}
		return false;
	}

	@Override
	public boolean handle(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			if (url.endsWith(".zaction")) {
				url = url.substring(0, url.length() - 8);
			}
			if (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}
			try {
				IMethodLocator method = MethodLocatorUtil.find(url.substring(1));
				if (method == null) {
					return false;
				}
				return invoke(request, response, method);
			} catch (UIMethodNotFoundException e) {
				return false;
			}
		} finally {
			BlockingTransaction.clearTransactionBinding();// 检测是否有未被关闭的阻塞型事务连接
		}
	}

	public static boolean invoke(HttpServletRequest request, HttpServletResponse response, IMethodLocator method) throws ServletException,
			IOException {
		PrivCheck.check(method);
		// 参数检查
		if (!VerifyCheck.check(method)) {
			String message = "Verify check failed:method=" + method + ",data=" + Current.getRequest();
			LogUtil.warn(message);
			Current.getResponse().setFailedMessage(message);
			response.getWriter().write(Current.getResponse().toXML());
			return true;
		}
		ZAction action = new ZAction();
		CookieData cookies = new CookieData(request);
		action.setCookies(cookies);
		action.setRequest(request);
		action.setResponse(response);
		try {
			if (Config.getServletMajorVersion() == 2 && Config.getServletMinorVersion() == 3) {
				response.setContentType("text/html;charset=" + Config.getGlobalCharset());
			} else {
				response.setContentType("text/html");
				response.setCharacterEncoding(Config.getGlobalCharset());
			}
			method.execute(new Object[] { action });
			if (!action.isBinaryMode()) {// 没有重定向，则输出内容
				try {
					response.getWriter().print(action.getContent());
				} catch (Exception e) {

				}// 不需要输出异常
			}
		} finally {
			if (action.isBinaryMode()) {
				response.getOutputStream().close();
			}
		}
		return true;
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public int getOrder() {
		return 9999;
	}

}
