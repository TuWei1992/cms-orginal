package com.zving.framework.utility.log;

import com.zving.framework.collection.Queuex;

/**
 * 日志Appender类，主要是为了实现在日志界面中获取实时日志。
 * 
 * @author 兰军
 * @mail lanjun@zving.com 2012-1-10
 */
public class LogAppender {
	private static long id = 0;

	private static Queuex<LogMessage> queue = new Queuex<LogMessage>(200);

	public void add(String message) {
		id++;
		LogMessage lm = new LogMessage();
		lm.id = id;
		lm.message = message + "\n";
		queue.push(lm);
	}

	/**
	 * 获取滚动日志
	 * 
	 * @return
	 */
	public static StringBuffer getLog(long id) {
		StringBuffer msg = new StringBuffer();
		for (int i = 0; i < queue.size(); i++) {
			LogMessage lm = queue.get(i);
			if (lm.id > id) {
				msg.append(lm.message);
			}
		}
		return msg;
	}

	/**
	 * 取到最大号
	 * 
	 * @return
	 */
	public static long getMaxId() {
		return id;
	}

	private class LogMessage {
		long id;
		String message;
	}
}
