package com.zving.framework.schedule;

import com.zving.framework.extend.IExtendItem;

/**
 * 任务虚拟类
 * 
 * @Author 王育春
 * @Date 2009-4-16
 * @Mail wyuch@zving.com
 */
public abstract class AbstractTask implements IExtendItem {
	/**
	 * Cron表达式
	 */
	public abstract String getCronExpression();

	/**
	 * 默认Cron表达式，未通过配置文件设置Cron表达式时使用此方法返回的Cron表达式
	 */
	public abstract String getDefaultCronExpression();

	/**
	 * 前端单独部署时是否可用
	 */
	public boolean enable4Front() {
		return false;
	}
}
