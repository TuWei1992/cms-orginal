package com.zving.framework.utility;

import com.zving.framework.core.FrameworkException;

/**
 * RegexParser匹配的过程中发现有固定字符串找不到，则直接抛出此异常以中止后续匹配
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-11-19
 */
public class RegexMatchFailedException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public RegexMatchFailedException(String message) {
		super(message);
	}

}
