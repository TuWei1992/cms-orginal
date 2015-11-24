package com.zving.framework.schedule;

import com.zving.framework.utility.StringUtil;

/**
 * 系统定时任务,可以在framework.xml配置是否执行及执行周期，例如：<br>
 * 
 * <pre>
 * &lt;framework&gt;
 *   &lt;cron&gt;
 *     &lt;task id="com.zving.framework.misc.FrameworkTask" disabled="true" time="0 2 * * *"/&gt;
 *   &lt;/cron&gt;
 * &lt;/framework&gt;
 * </pre>
 * 
 * 本类定时任务会在单独的线程中执行，并且上一次执行未完成下一次执行会自动忽略。
 * 
 * @Author 王育春
 * @Date 2008-12-8
 * @Mail wyuch@zving.com
 */
public abstract class SystemTask extends AbstractTask {
	protected String cronExpression;
	protected boolean disabled;
	protected boolean needRetry;
	protected int retryInterval;
	protected int retryTimes;

	/**
	 * 执行任务
	 */
	public abstract void execute();

	@Override
	public String getCronExpression() {
		if (StringUtil.isEmpty(cronExpression)) {
			cronExpression = getDefaultCronExpression();
		}
		return cronExpression;
	}

	public String setCronExpression(String expr) {
		return cronExpression = expr;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isNeedRetry() {
		return needRetry;
	}

	public void setNeedRetry(boolean needRetry) {
		this.needRetry = needRetry;
	}

	public int getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}
}
