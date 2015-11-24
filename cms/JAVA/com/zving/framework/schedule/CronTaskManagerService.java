package com.zving.framework.schedule;

import com.zving.framework.extend.AbstractExtendService;

/**
 * 任务管理器扩展服务类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-9
 */
public class CronTaskManagerService extends AbstractExtendService<AbstractTaskManager> {

	public static CronTaskManagerService getInstance() {
		return findInstance(CronTaskManagerService.class);
	}
}
