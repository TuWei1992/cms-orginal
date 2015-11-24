package com.zving.framework.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.zving.framework.Config;
import com.zving.framework.User;
import com.zving.framework.User.UserData;
import com.zving.framework.collection.ConcurrentMapx;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.action.AfterCronTaskExecutedAction;
import com.zving.framework.extend.action.BeforeCronTaskExecutedAction;
import com.zving.framework.thirdparty.commons.ArrayUtils;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 任务监听器，用于定期检测计划任务是否完成，若己完成，则添加新的计划任务
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2005-7-15
 */
public class CronMonitor extends TimerTask {

	private static Pattern P1 = Pattern.compile("\\d+", Pattern.DOTALL);// 指定数i

	private static Pattern P2 = Pattern.compile("\\d+\\-\\d+", Pattern.DOTALL);// 指定时间段a-b

	private static Pattern P3 = Pattern.compile("(((\\d+\\-\\d+)|\\d+)(,|$))+", Pattern.DOTALL);// 指定离散的几个值

	private static Pattern P4 = Pattern.compile("((\\d+\\-\\d+)|\\*)\\/\\d+", Pattern.DOTALL);// 步进表达式

	private static ThreadGroup cronThreadGroup = new ThreadGroup("CronTaskThreadGroup");

	private static CronMonitor instance = new CronMonitor();

	private boolean running = false;// 当前执行过程是否结束

	private ConcurrentMapx<String, Boolean> taskStatus = new ConcurrentMapx<String, Boolean>();

	private ExecutorService es = null;

	private CronMonitor() {
		es = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 120L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(cronThreadGroup, r);
			}
		});
	}

	public static CronMonitor getInstance() {
		return instance;
	}

	/**
	 * 本方法每秒执行一次
	 */
	@Override
	public void run() {
		if (!running) {
			running = true;
			try {
				// 为定时任务设定当前用户
				UserData u = new UserData();
				u.setBranchInnerCode("0000");
				u.setUserName("CronTask");
				u.setLogin(true);
				User.setCurrent(u);
				runMain();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				running = false;
			}
		}
	}

	/**
	 * 定期执行主流程. 若Cron任务定义文件有改动，则终止当前任务序列的执行，重新安排文件中定义的任务。<br>
	 * 若没有改动，则检查任务序列中有没有任务是刚执行完的，若有，则为其安排下次执行
	 */
	private void runMain() {
		final Date current = new Date();
		for (final AbstractTaskManager tm : CronTaskManagerService.getInstance().getAll()) {
			if (Config.isFrontDeploy() && !tm.enable4Front()) {
				continue;
			}
			Map<String, String> tmap = tm.getUsableTasks();
			for (final String id : tmap.keySet()) {
				try {
					if (isOnTime(current, tm.getTaskCronExpression(id))) {
						final String taskKey = tm.getExtendItemID() + "_" + id;
						if (taskStatus.getBoolean(taskKey)) {
							LogUtil.warn("Task " + id + " is on time ,but its last execution isn't finished!");
						} else {
							taskStatus.put(taskKey, true);
							es.submit(new Runnable() {
								@Override
								public void run() {
									try {
										ExtendManager.invoke(BeforeCronTaskExecutedAction.ID, new Object[] { tm.getExtendItemID(), id });
										tm.execute(id); // 扩展点,主要用于定时任务监测
										ExtendManager.invoke(AfterCronTaskExecutedAction.ID, new Object[] { tm.getExtendItemID(), id });
									} finally {
										taskStatus.put(taskKey, false);
									}
								}
							});
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 任务是否在运行
	 */
	public boolean isTaskRunning(String managerID, String taskID) {
		return taskStatus.getBoolean(taskID);
	}

	/**
	 * 将一个任务加入到执行队列，不管这个任务是否已经到了执行时间。如果这个任务当时正在执行，则不做任何操作
	 */
	public void executeTask(String managerID, final String id) {
		final String taskKey = managerID + "_" + id;
		if (taskStatus.getBoolean(taskKey)) {
			return;
		}
		final AbstractTaskManager tm = CronTaskManagerService.getInstance().get(managerID);
		taskStatus.put(taskKey, true);
		es.submit(new Runnable() {
			@Override
			public void run() {
				try {
					ExtendManager.invoke(BeforeCronTaskExecutedAction.ID, new Object[] { tm.getExtendItemID(), id });
					tm.execute(id); // 扩展点,主要用于定时任务监测
					ExtendManager.invoke(AfterCronTaskExecutedAction.ID, new Object[] { tm.getExtendItemID(), id });
				} finally {
					taskStatus.put(taskKey, false);
				}
			}
		});

	}

	/**
	 * 销毁对象
	 */
	public void destory() {
		cancel();
	}

	/**
	 * 判断当前时间是否已经到了执行该表达式的时间
	 * 
	 * @param date
	 * @param cronExpr
	 * @return
	 */
	public static boolean isOnTime(Date date, String cronExpr) {
		if (StringUtil.isNull(cronExpr)) {
			return false;
		}
		try {
			Date d = getNextRunTime(date, cronExpr);
			long t1 = d.getTime();
			long t2 = date.getTime();
			if (t1 / 1000 == t2 / 1000 && Math.abs(t1 - t2) < CronManager.SCAN_INTERVAL) {
				return true;
			}
		} catch (CronExpressionException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取表达式时间字段取值区间
	 * Second:0-59<br>
	 * Minute:0-59<br>
	 * Hour:0-23<br>
	 * Day:1-31<br>
	 * Month:1-12<br>
	 * Week:1-7<br>
	 */
	public static int[] getSuitNumbers(String exp, int min, int max) throws CronExpressionException {
		ArrayList<Integer> list = new ArrayList<Integer>();
		if (P1.matcher(exp).matches()) {
			int v = Integer.parseInt(exp);
			v = v > max ? max : v;
			v = v < min ? min : v;
			list.add(new Integer(v));
		} else if (P2.matcher(exp).matches()) {
			String[] arr = exp.split("\\-");
			int[] is = new int[] { Integer.parseInt(arr[0]), Integer.parseInt(arr[1]) };
			is[0] = is[0] > max ? max : is[0];
			is[1] = is[1] > max ? max : is[1];
			is[0] = is[0] < min ? min : is[0];
			is[1] = is[1] < min ? min : is[1];
			if (is[0] > is[1]) {
				for (int j = is[0]; j <= max; j++) {
					list.add(new Integer(j));
				}
				for (int j = min; j <= is[1]; j++) {
					list.add(new Integer(j));
				}
			} else {
				for (int j = is[0]; j <= is[1]; j++) {
					list.add(new Integer(j));
				}
			}
		} else if (P3.matcher(exp).matches()) {
			String[] arr = exp.split(",");
			for (String str : arr) {
				if (str.indexOf('-') > 0) {
					String[] arr2 = str.split("\\-");
					int[] tmp = new int[] { Integer.parseInt(arr2[0]), Integer.parseInt(arr2[1]) };
					tmp[0] = tmp[0] > max ? max : tmp[0];
					tmp[1] = tmp[1] > max ? max : tmp[1];
					tmp[0] = tmp[0] < min ? min : tmp[0];
					tmp[1] = tmp[1] < min ? min : tmp[1];
					if (tmp[0] > tmp[1]) {
						for (int j = tmp[0]; j <= max; j++) {
							list.add(new Integer(j));
						}
						for (int j = min; j <= tmp[1]; j++) {
							list.add(new Integer(j));
						}
					} else {
						for (int j = tmp[0]; j <= tmp[1]; j++) {
							list.add(new Integer(j));
						}
					}
				} else {
					list.add(new Integer(Integer.parseInt(str)));
				}
			}
		} else if (P4.matcher(exp).matches()) {
			String[] arr = exp.split("\\/");
			int step = Integer.parseInt(arr[1]);
			int[] is = new int[2];
			if (arr[0].equals("*")) {
				is[0] = min;
				is[1] = max;
			} else {
				arr = arr[0].split("\\-");
				is = new int[] { Integer.parseInt(arr[0]), Integer.parseInt(arr[1]) };
				is[0] = is[0] > max ? max : is[0];
				is[1] = is[1] > max ? max : is[1];
				is[0] = is[0] < min ? min : is[0];
				is[1] = is[1] < min ? min : is[1];
			}
			int cm = is[1];
			int len = max - min + 1;
			if (is[0] > is[1]) {
				cm = is[1] + len;
			}
			for (int i = is[0]; i <= cm; i += step) {
				list.add(new Integer(i > max ? i - len : i));
			}
		} else if (exp.equals("*")) {
			for (int i = min; i <= max; i++) {
				list.add(new Integer(i));
			}
		} else {
			throw new CronExpressionException("Invalid cron expression:" + exp);
		}
		int[] arr = new int[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i).intValue();
		}
		Arrays.sort(arr);
		return arr;
	}

	/**
	 * 根据cron表达式获取下一次执行的时间
	 * 
	 * @param lastDate 最后执行时间或当前时间
	 * @param cronExpression
	 * @return
	 * @throws CronExpressionException
	 */
	public static Date getNextRunTime(Date lastDate, String cronExpression) throws CronExpressionException {
		if (StringUtil.isEmpty(cronExpression)) {
			throw new CronExpressionException("Invalid cron expression:" + cronExpression);
		}
		String[] expArr = cronExpression.split("\\s");
		if (expArr.length < 5) {
			throw new CronExpressionException("Invalid cron expression:" + cronExpression);
		} else if (expArr.length == 5) {// 需要加入秒钟
			expArr = ArrayUtils.add(expArr, 0, "0");// 默认为零秒
		}
		Calendar c = Calendar.getInstance();
		c.setTime(lastDate);

		// 月份
		int month = c.get(Calendar.MONTH) + 1;
		int[] ms = getSuitNumbers(expArr[4], 1, 12);
		int mi = -1;
		boolean carryFlag = false;// 进位标志，如果己进位，则下一单位可以随便取一个数字
		boolean flag = false;
		for (int i = 0; i < ms.length; i++) {
			if (ms[i] == month) {
				mi = i;
				flag = true;
				break;
			}
			if (ms[i] > month) {
				c.set(Calendar.MONTH, ms[i] - 1);
				carryFlag = true;
				mi = i;
				flag = true;
				break;
			}
		}
		if (!flag) {
			mi = 0;
			c.set(Calendar.MONTH, ms[mi] - 1);
			c.add(Calendar.YEAR, 1);
			carryFlag = true;
		}

		// 天数
		int day = c.get(Calendar.DAY_OF_MONTH);
		int[] ds = getSuitNumbers(expArr[3], 1, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		int di = -1;
		if (carryFlag) {
			di = 0;
			c.set(Calendar.DAY_OF_MONTH, ds[0]);
		} else {
			flag = false;
			for (int i = 0; i < ds.length; i++) {
				if (ds[i] == day) {
					di = i;
					flag = true;
					break;
				}
				if (ds[i] > day) {
					c.set(Calendar.DAY_OF_MONTH, ds[i]);
					carryFlag = true;
					di = i;
					flag = true;
					break;
				}
			}
			if (!flag) {
				c.set(Calendar.DAY_OF_MONTH, ds[0]);
				if (mi != ms.length - 1) {
					mi++;
				} else {
					mi = 0;
					c.add(Calendar.YEAR, 1);
				}
				c.set(Calendar.MONTH, ms[mi] - 1);
				carryFlag = true;
				di = 0;
			}
		}

		// 小时
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int[] hs = getSuitNumbers(expArr[2], 0, 23);
		int hi = -1;
		if (carryFlag) {
			hi = 0;
			c.set(Calendar.HOUR_OF_DAY, hs[0]);
		} else {
			flag = false;
			for (int i = 0; i < hs.length; i++) {
				if (hs[i] == hour) {
					hi = i;
					flag = true;
					break;
				}
				if (hs[i] > hour) {
					c.set(Calendar.HOUR_OF_DAY, hs[i]);
					carryFlag = true;
					hi = i;
					flag = true;
					break;
				}
			}
			if (!flag) {
				c.set(Calendar.HOUR_OF_DAY, hs[0]);
				if (di != ds.length - 1) {
					di++;
				} else {
					di = 0;
					if (mi != ms.length - 1) {
						mi++;
					} else {
						mi = 0;
						c.add(Calendar.YEAR, 1);
					}
					c.set(Calendar.MONTH, ms[mi] - 1);
				}
				c.set(Calendar.DAY_OF_MONTH, ds[di]);
				carryFlag = true;
				hi = 0;
			}
		}

		// 分钟
		int minute = c.get(Calendar.MINUTE);
		int fi = -1;// 分钟为第几个可用数字
		int[] fs = getSuitNumbers(expArr[1], 0, 59);
		if (carryFlag) {
			fi = 0;
			c.set(Calendar.MINUTE, fs[0]);
		} else {
			flag = false;
			for (int i = 0; i < fs.length; i++) {
				if (fs[i] == minute) {
					flag = true;
					fi = i;
					break;
				}
				if (fs[i] > minute) {
					c.set(Calendar.MINUTE, fs[i]);
					fi = i;
					carryFlag = true;
					flag = true;
					break;
				}
			}
			if (!flag) {
				c.set(Calendar.MINUTE, fs[0]);
				fi = 0;
				if (hi != hs.length - 1) {
					hi++;
				} else {
					if (di != ds.length - 1) {
						di++;
					} else {
						di = 0;
						if (mi != ms.length - 1) {
							mi++;
						} else {
							mi = 0;
							c.add(Calendar.YEAR, 1);
						}
						c.set(Calendar.MONTH, ms[mi] - 1);
					}
					hi = 0;
				}
				c.set(Calendar.HOUR_OF_DAY, hs[hi]);
				carryFlag = true;
			}
		}

		// 秒钟
		int second = c.get(Calendar.SECOND);
		int[] ss = getSuitNumbers(expArr[0], 0, 59);
		if (carryFlag) {
			c.set(Calendar.SECOND, ss[0]);
		} else {
			flag = false;
			for (int element : ss) {
				if (element == second) {
					flag = true;
					break;
				}
				if (element > second) {
					c.set(Calendar.SECOND, element);
					carryFlag = true;
					flag = true;
					break;
				}
			}
			if (!flag) {
				c.set(Calendar.SECOND, ss[0]);
				if (fi != fs.length - 1) {
					fi++;
				} else {
					if (hi != hs.length - 1) {
						hi++;
					} else {
						if (di != ds.length - 1) {
							di++;
						} else {
							di = 0;
							if (mi != ms.length - 1) {
								mi++;
							} else {
								mi = 0;
								c.add(Calendar.YEAR, 1);
							}
							c.set(Calendar.MONTH, ms[mi] - 1);
						}
						hi = 0;
					}
				}
				c.set(Calendar.MINUTE, fs[fi]);
				carryFlag = true;
			}
		}
		c.set(Calendar.DAY_OF_MONTH, ds[di]);// di可能发生了变化

		// 星期
		int week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (week == 0) {
			week = 7;
		}
		int[] ws = getSuitNumbers(expArr[5], 1, 7);
		flag = false;
		for (int element : ws) {
			if (element == week) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			c.add(Calendar.DAY_OF_MONTH, 1);
			return getNextRunTime(c.getTime(), cronExpression);
		}
		return new Date(c.getTimeInMillis() - c.getTimeInMillis() % 1000);// 只保留秒数
	}

	/**
	 * 根据表达式获取下次执行时间
	 * 
	 * @param cronExpression
	 * @return
	 * @throws CronExpressionException
	 */
	public static Date getNextRunTime(String cronExpression) throws CronExpressionException {
		return getNextRunTime(new Date(), cronExpression);
	}

	/**
	 * 判断当前线程是否是一个定时任务线程
	 */
	public static boolean isCronThread() {
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		while (tg != null) {
			if (tg == cronThreadGroup) {
				return true;
			}
			tg = tg.getParent();
		}
		return false;
	}
}
