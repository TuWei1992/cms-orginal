package com.zving.framework.utility;

import java.util.ArrayList;

import com.zving.framework.Current;

/**
 * 批量操作时存储所有的错误信息
 * 
 * @Author 王育春
 * @Date 2006-12-13
 * @Mail wyuch@midding.com
 */
public class Errorx {
	protected ArrayList<Message> list = new ArrayList<Message>();

	protected boolean ErrorFlag = false;

	protected boolean ErrorDealedFlag = true;

	/**
	 * 增加一条普通消息
	 * 
	 * @param message
	 */
	public static void addMessage(String message) {
		add(message, false);
	}

	/**
	 * 增加一条错误消息
	 * 
	 * @param message
	 */
	public static void addError(String message) {
		add(message, true);
	}

	/**
	 * 增加一条消息，isError为true表示是错误消息
	 * 
	 * @param message
	 * @param isError
	 */
	private static void add(String message, boolean isError) {
		Message msg = new Message();
		msg.isError = isError;
		msg.Message = message;
		if (isError) {
			getCurrent().ErrorFlag = true;
			getCurrent().ErrorDealedFlag = false;
			StackTraceElement stack[] = new Throwable().getStackTrace();
			StringBuilder sb = new StringBuilder();
			sb.append("Errorx : ");
			sb.append(message);
			sb.append("\n");
			for (int i = 2; i < stack.length; i++) {// 前两个是Errorx里的方法，过滤
				StackTraceElement ste = stack[i];
				if (ste.getClassName().indexOf("DBConnPool") == -1) {
					sb.append("\tat ");
					sb.append(ste.getClassName());
					sb.append(".");
					sb.append(ste.getMethodName());
					sb.append("(");
					sb.append(ste.getFileName());
					sb.append(":");
					sb.append(ste.getLineNumber());
					sb.append(")\n");
				}
			}
		}
		getCurrent().list.add(msg);
	}

	/**
	 * 是否有错误消息
	 * 
	 * @return
	 */
	public static boolean hasError() {
		return getCurrent().ErrorFlag;
	}

	/**
	 * 是否所有错误消息都已经被处理过了
	 * 
	 * @return
	 */
	public static boolean hasDealed() {
		return getCurrent().ErrorDealedFlag;
	}

	/**
	 * 清除所有消息
	 */
	public static void clear() {
		Errorx x = getCurrent();
		if (x.list.size() > 64) {
			x.list = new ArrayList<Message>();
		} else {
			x.list.clear();
		}
		x.list.clear();
		x.ErrorFlag = false;
		x.ErrorDealedFlag = true;
	}

	/**
	 * 转换所有错误消息为可读的形式
	 * 
	 * @return
	 */
	public static String printString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getCurrent().list.size(); i++) {
			Message msg = getCurrent().list.get(i);
			if (msg.isError) {
				sb.append("Error:");
				sb.append(msg.Message);
				sb.append("<br>\n");
			}
		}
		for (int i = 0; i < getCurrent().list.size(); i++) {
			Message msg = getCurrent().list.get(i);
			if (!msg.isError) {
				sb.append("Warning:");
				sb.append(msg.Message);
				sb.append("\n");
			}
		}
		getCurrent().ErrorDealedFlag = true;
		return sb.toString();
	}

	/**
	 * 返回所有的消息以字符串的形式
	 * 
	 * @return
	 */
	public static String[] getMessages() {
		String[] arr = new String[getCurrent().list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = getCurrent().list.get(i).Message;
		}
		clear();
		return arr;
	}

	/**
	 * 以文本形式返回所有消息
	 */
	public static String getAllMessage() {
		FastStringBuilder sb = new FastStringBuilder();
		int index = 1;
		for (int i = 0; i < getCurrent().list.size(); i++) {
			Message msg = getCurrent().list.get(i);
			if (msg.isError) {
				sb.append("\n").append(index).append(". Error: ").append(msg.Message);
				index++;
			} else {
				sb.append("\n").append(index).append(". Warning: ").append(msg.Message);
				index++;
			}
		}
		clear();
		return sb.toStringAndClose();
	}

	/**
	 * 返回当前的Errorx对象
	 * 
	 * @return
	 */
	public static Errorx getCurrent() {
		return Current.getErrorx();
	}

	/**
	 * 消息对象类
	 * 
	 * @author 王育春
	 * @date 2009-11-15
	 * @email wangyc@zving.com
	 */
	public static class Message {
		/**
		 * 是否是错误
		 */
		public boolean isError;

		/**
		 * 消息内容
		 */
		public String Message;
	}
}
