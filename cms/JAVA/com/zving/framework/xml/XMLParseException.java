package com.zving.framework.xml;

import com.zving.framework.core.FrameworkException;

/**
 * XML解析异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-10
 */
public class XMLParseException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public XMLParseException(String message) {
		super(message);
	}

	public XMLParseException(Throwable t) {
		super(t);
	}

}
