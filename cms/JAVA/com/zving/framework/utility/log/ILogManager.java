package com.zving.framework.utility.log;

/**
 * 日志管理器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-10-28
 */
public interface ILogManager {
	/**
	 * 输出到控制台的日志记录器
	 */
	public ILogger getConsoleLogger();

	/**
	 * 输出到错误日志的日志记录器
	 */
	public ILogger getErrorLogger();

	/**
	 * 输出到定时任务日志的日志记录器
	 */
	public ILogger getCronLogger();
}
