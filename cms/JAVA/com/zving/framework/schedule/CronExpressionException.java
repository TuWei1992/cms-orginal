package com.zving.framework.schedule;

/**
 * Cron表达式异常
 * 
 * @Author 王育春
 * @Date 2008-7-24
 * @Mail wyuch@zving.com
 */
public class CronExpressionException extends Exception {
	private static final long serialVersionUID = 1L;

	public CronExpressionException(String message) {
		super(message);
	}
}
