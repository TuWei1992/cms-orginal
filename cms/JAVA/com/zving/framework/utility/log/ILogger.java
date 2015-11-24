package com.zving.framework.utility.log;

/**
 * 日志记录器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-10-28
 */
public interface ILogger {

	public void trace(Object paramObject);

	public void debug(Object paramObject);

	public void info(Object paramObject);

	public void warn(Object paramObject);

	public void error(Object paramObject);

	public void fatal(Object paramObject);
}
