package com.zving.framework.utility.log;

/**
 * 将日志全部输出到控制台的日志管理器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-10-28
 */
public class ConsoleLogManager implements ILogManager {
	@Override
	public ILogger getConsoleLogger() {
		return new ConsoleLogger(false);
	}

	@Override
	public ILogger getErrorLogger() {
		return new ConsoleLogger(true);
	}

	@Override
	public ILogger getCronLogger() {
		return new ConsoleLogger(false);
	}

}
