package com.zving.framework.ui.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.User;
import com.zving.framework.User.UserData;
import com.zving.framework.collection.CacheMapx;
import com.zving.framework.i18n.LangMapping;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringFormat;
import com.zving.framework.utility.StringUtil;

/**
 * 长时间任务类。<br>
 * 长时间运行的任务需要单独开辟线程，并向前台报告进度
 * 
 * @Author 王育春
 * @Date 2008-7-25
 * @Mail wyuch@zving.com
 */
public abstract class LongTimeTask extends Thread {
	private static CacheMapx<Long, LongTimeTask> map = new CacheMapx<Long, LongTimeTask>();

	private static long IDBase = System.currentTimeMillis();

	private static final int MaxListSize = 1000;

	private long id;

	private ArrayList<String> list = new ArrayList<String>();

	protected int percent;

	protected String currentInfo;

	private String finishedInfo; // 允许前台设置任务结束时的提示信息

	protected ArrayList<String> errors = new ArrayList<String>();

	private boolean stopFlag;

	private UserData user;

	private String type;// 用来标识同一个来源的任务

	private long stopTime = System.currentTimeMillis() + 1000 * 60 * 24;

	private static ReentrantLock lock = new ReentrantLock();

	public static LongTimeTask createEmptyInstance() {// 构造一个实例，此实例仅用于避免空指针
		return new LongTimeTask(false) {
			@Override
			public void execute() {
			}
		};
	}

	public static LongTimeTask getInstanceById(long id) {
		return map.get(new Long(id));
	}

	public static Collection<LongTimeTask> getAllInstance() {
		return map.values();
	}

	public static void removeInstanceById(long id) {
		map.remove(new Long(id));
	}

	/**
	 * 中止指定type的任务
	 */
	public static String cancelByType(String type) {
		String message = LangMapping.get("Framework.Task.NotExistTaskOfType") + ":" + type;
		LongTimeTask ltt = getInstanceByType(type);
		if (ltt != null) {
			ltt.stopTask();
			message = LangMapping.get("Framework.Task.TaskStopping");
		}
		return message;
	}

	/**
	 * 根据type查找指定任务，一个type的任务同时只能有一个在运行
	 */
	public static LongTimeTask getInstanceByType(String type) {
		if (StringUtil.isNotEmpty(type)) {
			long current = System.currentTimeMillis();
			for (Long key : map.keySet()) {
				LongTimeTask ltt = map.get(key);
				if (type.equals(ltt.getType())) {
					if (current - ltt.stopTime > 60000) {
						map.remove(key);
						return null;
					}
					return ltt;
				}
			}
		}
		return null;
	}

	public LongTimeTask() {
		this(true);
	}

	private LongTimeTask(boolean flag) {// flag为false时构造一个仅用来避免空指针的任务
		if (flag) {
			setName("LongTimeTask Thread");
			id = IDBase++;
			map.put(new Long(id), this);
			clearStopedTask();
		}
	}

	private void clearStopedTask() {
		lock.lock();
		try {
			long current = System.currentTimeMillis();
			for (Long k : map.keySet()) {
				LongTimeTask ltt = map.get(k);
				if (current - ltt.stopTime > 60000) {
					map.remove(k);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public long getTaskID() {
		return id;
	}

	public void info(String message) {
		LogUtil.info(message);
		list.add(message);
		if (list.size() > MaxListSize) {
			list.remove(0);
		}
	}

	public String[] getMessages() {
		String[] arr = new String[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i);
		}
		list.clear();
		return arr;
	}

	@Override
	public void run() {
		if (StringUtil.isNotEmpty(type)) {
			LongTimeTask ltt = getInstanceByType(type);
			if (ltt != null && ltt != this) {
				return;
			}
		}
		try {
			User.setCurrent(user);
			execute();
		} catch (StopThreadException ie) {
			interrupt();
		} finally {
			stopTime = System.currentTimeMillis();
		}
	}

	public abstract void execute();

	public boolean checkStop() {
		return stopFlag;
	}

	public void stopTask() {
		clearStopedTask();
		stopFlag = true;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public void setCurrentInfo(String currentInfo) {
		this.currentInfo = currentInfo;
		LogUtil.info(currentInfo);
	}

	public String getCurrentInfo() {
		return currentInfo;
	}

	public void setFinishedInfo(String finishedInfo) {
		this.setPercent(100);
		this.finishedInfo = finishedInfo;
		LogUtil.info(finishedInfo);
	}

	public String getFinishedInfo() {
		return finishedInfo;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public void addError(String error) {
		errors.add(error);
	}

	public String getAllErrors() {
		if (errors.size() == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		StringFormat sf = new StringFormat(LangMapping.get("Framework.Task.TotleErrors"), errors.size());
		sb.append(sf.toString() + ":<br>");
		for (int i = 0; i < errors.size(); i++) {
			sb.append(i + 1);
			sb.append(": ");
			sb.append(errors.get(i));
			sb.append("<br>");
		}
		return sb.toString();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
