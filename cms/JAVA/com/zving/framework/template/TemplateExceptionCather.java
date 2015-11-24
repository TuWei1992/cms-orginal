package com.zving.framework.template;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zving.framework.core.IExceptionCatcher;
import com.zving.framework.template.exception.TemplateException;
import com.zving.framework.utility.LogUtil;

/**
 * 模板异常捕获器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-4-3
 */
public class TemplateExceptionCather implements IExceptionCatcher {

	@Override
	public String getExtendItemID() {
		return "com.zving.framework.template.TemplateExceptionCather";
	}

	@Override
	public String getExtendItemName() {
		return "Default Template Exception Catcher";
	}

	@Override
	public Class<?>[] getTargetExceptionClass() {
		return new Class<?>[] { TemplateException.class };
	}

	@Override
	public void doCatch(RuntimeException e, HttpServletRequest request, HttpServletResponse response) {
		LogUtil.error("TemplateException found in " + request.getRequestURL());
		throw e;
	}

}
