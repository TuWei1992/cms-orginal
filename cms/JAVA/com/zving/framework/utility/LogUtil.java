package com.zving.framework.utility;

import java.io.OutputStream;
import java.io.PrintStream;

import com.zving.framework.Config;
import com.zving.framework.config.LogManagerClass;
import com.zving.framework.schedule.CronMonitor;
import com.zving.framework.utility.log.ILogManager;
import com.zving.framework.utility.log.ILogger;
import com.zving.framework.utility.log.LogAppender;

/**
 * 日志工具类
 * 
 * @Author 王育春
 * @Date 2007-6-19
 * @Mail wyuch@zving.com
 */
public class LogUtil {
	private static boolean initFlag = false;

	private static ILogger cron = null;

	private static ILogger console = null;

	private static ILogger error = null;

	private static LogAppender appender = new LogAppender();

	private static ILogManager manager = null;

	private static PrintStream syserr;

	private synchronized static void init() {
		if (initFlag) {
			return;
		}
		if (!(System.err instanceof Log4jErrorPrintStream)) {
			syserr = System.err;
		}
		try {
			if (ObjectUtil.empty(Config.getPluginPath())) {
				System.err.println("LogUtil cann't get plugins path.");
			} else {
				Log4jErrorPrintStream logStream = new Log4jErrorPrintStream(System.err);
				System.setErr(logStream);
				String className = LogManagerClass.getValue();
				manager = (ILogManager) Class.forName(className).newInstance();
				cron = manager.getCronLogger();
				error = manager.getErrorLogger();
				console = manager.getConsoleLogger();
			}
		} catch (Exception e) {
			System.setErr(syserr);// 加载出错时恢复默认情况
			e.printStackTrace();
		} finally {
			initFlag = true;
		}
	}

	private static ILogger getLogger() {
		init();
		if (CronMonitor.isCronThread()) {
			return cron;
		} else {
			return console;
		}
	}

	private static void append(String type, Object obj) {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append(": ");
		sb.append(DateUtil.getCurrentDateTime());
		sb.append(' ');
		sb.append(Config.getAppCode());
		sb.append(' ');
		sb.append(obj);
		appender.add(sb.toString());
	}

	public static void info(Object obj) {
		ILogger log = getLogger();
		if (log == null) {
			System.out.println(obj);
			return;
		}
		log.info(Config.getAppCode() + " " + obj);
		append("INFO", obj);
	}

	public static void debug(Object obj) {
		ILogger log = getLogger();
		if (log == null) {
			System.out.println(obj);
			return;
		}
		log.debug(Config.getAppCode() + " " + obj);
		append("DEBUG", obj);
	}

	public static void warn(Object obj) {
		ILogger log = getLogger();
		if (log == null) {
			System.err.println(obj);
			return;
		}
		log.warn(Config.getAppCode() + " " + obj);
		append("WARN", obj);
	}

	public static void error(Object obj) {
		ILogger log = getLogger();
		if (log == null) {
			System.err.println(obj);
			return;
		}
		log.error(Config.getAppCode() + " " + obj);
		append("ERROR", obj);
	}

	public static void fatal(Object obj) {
		ILogger log = getLogger();
		if (log == null) {
			System.err.println(obj);
			return;
		}
		log.fatal(Config.getAppCode() + " " + obj);
		append("FATAL", obj);
	}

	static class Log4jErrorPrintStream extends PrintStream {
		boolean startFlag = false;// 开始输出日志，主要是为了避免一些情况下递归输出错误的BUG

		Log4jErrorPrintStream(OutputStream out) {
			super(out, true); // 使用自动刷新
		}

		@Override
		public void println(String obj) {
			println((Object) obj);
		}

		@Override
		public void println(Object obj) {
			if (startFlag) {
				return;
			}
			startFlag = true;
			try {
				if (error != null) {
					error.error(obj);
					appender.add("ERROR: " + DateUtil.getCurrentDateTime() + " " + obj);
				}
			} catch (Throwable e) {
				System.out.println("LogUtil.Log4jErrorPrintStream.println() failed:" + e.getMessage());
			} finally {
				startFlag = false;
			}
		}
	}

	public static PrintStream getSyserr() {
		return syserr;
	}
}
