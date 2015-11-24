package com.zving.framework.core.handler;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.CookieData;
import com.zving.framework.Current;
import com.zving.framework.ResponseData;
import com.zving.framework.config.LoginMethod;
import com.zving.framework.core.Dispatcher.DispatchException;
import com.zving.framework.core.exception.UIMethodNotFoundException;
import com.zving.framework.core.method.IMethodLocator;
import com.zving.framework.core.method.MethodLocatorUtil;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.action.AfterUIMethodInvokeAction;
import com.zving.framework.extend.action.BeforeUIMethodInvokeAction;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.security.VerifyCheck;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;

/**
 * Ajax请求处理者
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-11-5
 */
public class AjaxHandler extends AbstractHtmlHandler {
	public static final String ID = "com.zving.framework.core.AjaxHandler";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public boolean match(String url) {
		return url.startsWith("/ajax/invoke");
	}

	@Override
	public String getExtendItemName() {
		return "Ajax Server Invoke Processor";
	}

	@Override
	public boolean execute(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// resultDataFormat可能为以下几个值之一
		// json,jsonp,html,xml,text,script,setWindowName,postWindowMessage
		String resultDataFormat = request.getParameter(Constant.DataFormat);
		String data = request.getParameter(Constant.Data);
		String method = request.getParameter(Constant.Method);
		if (resultDataFormat == null) {
			if (data.startsWith("<")) {
				resultDataFormat = "xml";
			} else {
				resultDataFormat = "json";
			}
		}
		try {
			response.setContentType("text/html");

			if (Config.getServletMajorVersion() == 2 && Config.getServletMinorVersion() == 3) {
				response.setContentType("text/html;charset=utf-8");
			}
			request.setCharacterEncoding("UTF-8");

			if ("/index.zhtml".equals(request.getServletPath())) {// 重定向到Login.zhtml,以适应所有页面都打包到了ui.jar的情况
				response.sendRedirect("login.zhtml");
				return true;
			}
			IMethodLocator ml = MethodLocatorUtil.find(method);
			if (ml == null) {
				throw new UIMethodNotFoundException(method);
			}

			if ("".equals(url) || "/".equals(url)) {
				url = "/index.zhtml";
			}

			if (StringUtil.isEmpty(method)) {
				LogUtil.warn("Error in Server.sendRequest(),QueryString=" + request.getQueryString() + ",Referer="
						+ request.getHeader("referer"));
				return true;
			}
			PrivCheck.check(ml);

			// 参数检查
			if (!VerifyCheck.check(ml)) {
				String message = "Verify check failed:method=" + method + ",data=" + Current.getRequest();
				LogUtil.warn(message);
				Current.getResponse().setFailedMessage(message);
				write(resultDataFormat, response);
				return true;
			}

			// UIFacade方法执行前扩展
			ExtendManager.invoke(BeforeUIMethodInvokeAction.ExtendPointID, new Object[] { method });

			// 执行方法，并传入合适的参数
			Method m = ml.getMethod();
			Class<?>[] cs = m.getParameterTypes();
			Boolean isAction = false;
			ZAction action = null;
			// 如果调用的是ZAction方法
			if (cs.length > 0 && cs[0].isAssignableFrom(ZAction.class)) {
				isAction = true;
				action = new ZAction();
				CookieData cookies = new CookieData(request);
				action.setCookies(cookies);
				action.setRequest(request);
				action.setResponse(response);
				ml.execute(new Object[] { action });
			} else {
				ml.execute();
			}

			// UIFacade方法执行后扩展
			ExtendManager.invoke(AfterUIMethodInvokeAction.ExtendPointID, new Object[] { method });

			// 登录后得确保产生session
			if (LoginMethod.isLoginMethod(ml.getName())) {
				request.getSession(true);
			}

			// 将结果返回给页面
			if (isAction) {
				write(resultDataFormat, response, action);
			} else {
				write(resultDataFormat, response);
			}
		} catch (DispatchException e) {
			ResponseData r = new ResponseData();
			String redirectURL = Current.getDispatcher().getRedirectURL();
			if (redirectURL == null) {
				redirectURL = Current.getDispatcher().getForwardURL();
			}
			r.put(Constant.ResponseScriptAttr, "window.location.href='" + Config.getContextPath() + redirectURL + "';");
			write(resultDataFormat, response);
		}
		return true;
	}

	private void write(String resultDataFormat, HttpServletResponse response) throws IOException {
		StringBuilder sb = new StringBuilder();
		if (resultDataFormat.equalsIgnoreCase("setWindowName")) {
			response.setContentType("text/html");
			sb.append("<html><head>");
			sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=");
			sb.append(Config.getGlobalCharset());
			sb.append("\">");
			sb.append("</head><body>");
			sb.append("<script id=\"jsonData\" type=\"text/json\">");
			sb.append(Current.getResponse().toJSON());
			sb.append("</script>");
			sb.append("<script>window.name=document.getElementById('jsonData').innerHTML;</script>");
			sb.append("</body></html>");
		} else if (resultDataFormat.equalsIgnoreCase("json")) {
			response.setContentType("application/json");
			sb.append(Current.getResponse().toJSON());
		} else if (resultDataFormat.equalsIgnoreCase("xml")) {
			response.setContentType("text/xml");
			sb.append(Current.getResponse().toXML());
		}
		response.getWriter().write(sb.toString());
	}

	private void write(String resultDataFormat, HttpServletResponse response, ZAction action) throws IOException {
		if (resultDataFormat.equalsIgnoreCase("html")) {
			response.setContentType("text/html");
		} else if (resultDataFormat.equalsIgnoreCase("js")) {
			response.setContentType("text/javascript");
		} else if (resultDataFormat.equalsIgnoreCase("json")) {
			response.setContentType("application/json");
		}
		response.getWriter().print(action.getContent());
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public int getOrder() {
		return 9998;
	}

}
