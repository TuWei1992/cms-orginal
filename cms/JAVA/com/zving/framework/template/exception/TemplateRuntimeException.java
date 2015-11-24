package com.zving.framework.template.exception;

import com.zving.framework.i18n.Lang;
import com.zving.framework.template.AbstractTag;

/**
 * 模板运行时异常
 * 
 * @Author 王育春
 * @Date 2010-8-4
 * @Mail wyuch@zving.com
 */
public class TemplateRuntimeException extends TemplateException {

	private static final long serialVersionUID = 1L;

	public TemplateRuntimeException(String message) {
		super(message);
	}

	public TemplateRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TemplateRuntimeException(Throwable cause) {
		super(cause);
	}

	public TemplateRuntimeException(String message, AbstractTag tag) {
		super((tag == null ? "" : tag.getUrlFile() + Lang.get("Staticize.ErrorOnLine", tag.getStartLineNo())) + message);
	}

}
