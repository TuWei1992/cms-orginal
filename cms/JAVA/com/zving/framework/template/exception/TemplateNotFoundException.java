package com.zving.framework.template.exception;

/**
 * 模板未找到异常
 * 
 * @Author 王育春
 * @Date 2010-8-4
 * @Mail wyuch@zving.com
 */
public class TemplateNotFoundException extends TemplateException {

	private static final long serialVersionUID = 1L;

	public TemplateNotFoundException(String message) {
		super(message);
	}
}
