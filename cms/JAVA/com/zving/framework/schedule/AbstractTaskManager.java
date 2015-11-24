package com.zving.framework.schedule;

import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.IExtendItem;

/**
 * 任务管理器虚拟类
 * 
 * @Author 王育春
 * @Date 2008-7-22
 * @Mail wyuch@zving.com
 */
public abstract class AbstractTaskManager implements IExtendItem {
	/**
	 * 返回已经设置定时计划的任务
	 */
	public abstract Mapx<String, String> getUsableTasks();

	/**
	 * 返回所有任务
	 */
	public abstract Mapx<String, String> getConfigEnableTasks();

	/**
	 * 返回某个任务的Cron表达式
	 */
	public abstract String getTaskCronExpression(String id);

	/**
	 * 执行指定id的任务
	 */
	public abstract void execute(String id);

	/**
	 * 是否在前端部署时可用
	 */
	public boolean enable4Front() {
		return false;
	}
}