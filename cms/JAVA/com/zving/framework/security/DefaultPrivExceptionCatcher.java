package com.zving.framework.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.Config;
import com.zving.framework.Constant;
import com.zving.framework.Current;
import com.zving.framework.ResponseData;
import com.zving.framework.core.IExceptionCatcher;
import com.zving.framework.core.handler.AjaxHandler;
import com.zving.framework.security.exception.MemberNotLoginException;
import com.zving.framework.security.exception.PrivException;
import com.zving.framework.security.exception.UserNotLoginException;
import com.zving.framework.utility.ObjectUtil;

/**
 * 权限异常捕获器，捕获权限异常并决定跳转到哪个页面
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-6
 */
public class DefaultPrivExceptionCatcher implements IExceptionCatcher {
	public static final String ID = "com.zving.framework.security.DefaultPrivExceptionCatcher";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "Default PrivException Catcher";
	}

	@Override
	public Class<?>[] getTargetExceptionClass() {
		return new Class<?>[] { PrivException.class };
	}

	@Override
	public void doCatch(RuntimeException e, HttpServletRequest request, HttpServletResponse response) {
		try {
			if (e instanceof UserNotLoginException) {
				String redirectURL = Config.getContextPath() + Config.getLoginPage();
				if (Current.getURLHandler() instanceof AjaxHandler) {
					ResponseData r = new ResponseData();
					r.put(Constant.ResponseScriptAttr, "window.location.href='" + redirectURL + "';");
					response.getWriter().write(r.toXML());
				} else {
					response.sendRedirect(redirectURL);
				}
			} else if (!(e instanceof MemberNotLoginException)) {
				String redirectURL = Config.getContextPath() + "framework/noPrivilege.zhtml?url=";
				if (Current.getURLHandler() instanceof AjaxHandler) {
					String method = request.getParameter(Constant.Method);
					if (ObjectUtil.notEmpty(method)) {
						redirectURL += "ajax!" + method;
					}
					if (ObjectUtil.notEmpty(request.getQueryString())) {
						redirectURL += "&" + request.getQueryString();
					}
					ResponseData r = new ResponseData();
					r.put(Constant.ResponseScriptAttr, "window.location.href='" + redirectURL + "';");
					response.getWriter().write(r.toXML());
				} else {
					redirectURL += request.getRequestURI();
					if (ObjectUtil.notEmpty(request.getQueryString())) {
						redirectURL += "&" + request.getQueryString();
					}
					response.sendRedirect(redirectURL);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
