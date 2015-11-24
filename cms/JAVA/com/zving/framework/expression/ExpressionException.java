package com.zving.framework.expression;

/**
 * 表达式异常
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-20
 */
public class ExpressionException extends Exception {
	private static final long serialVersionUID = 1L;
	private Throwable mRootCause;

	public ExpressionException() {
	}

	public ExpressionException(String pMessage) {
		super(pMessage);
	}

	public ExpressionException(Throwable pRootCause) {
		super(pRootCause.getLocalizedMessage());
		mRootCause = pRootCause;
	}

	public ExpressionException(String pMessage, Throwable pRootCause) {
		super(pMessage);
		mRootCause = pRootCause;
	}

	public Throwable getRootCause() {
		return mRootCause;
	}
}
