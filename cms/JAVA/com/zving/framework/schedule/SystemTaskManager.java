package com.zving.framework.schedule;

import java.util.List;

import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.FrameworkException;

/**
 * 系统定时任务管理器,执行计划由任务实现类提供,不保存执行计划到持久层
 * 
 * @Author 王育春
 * @Date 2008-12-8
 * @Mail wyuch@zving.com
 */
public class SystemTaskManager extends AbstractTaskManager {
	public static final String ID = "SYSTEM";

	@Override
	public void execute(final String id) {
		SystemTask gt = SystemTaskService.getInstance().get(id);
		if (gt != null) {
			gt.execute();
		} else {
			throw new FrameworkException("Task not found:" + id);
		}
	}

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Framework.SystemTask}";
	}

	public SystemTask getTask(String id) {
		return SystemTaskService.getInstance().get(id);
	}

	public List<SystemTask> getAllTask() {
		return SystemTaskService.getInstance().getAll();
	}

	@Override
	public Mapx<String, String> getUsableTasks() {
		Mapx<String, String> map = new Mapx<String, String>();
		for (SystemTask gt : SystemTaskService.getInstance().getAll()) {
			if (Config.isFrontDeploy() && !gt.enable4Front()) {
				continue;
			}
			if (!gt.isDisabled()) {
				map.put(gt.getExtendItemID(), gt.getExtendItemName());
			}
		}
		return map;
	}

	@Override
	public String getTaskCronExpression(String id) {
		SystemTask gt = SystemTaskService.getInstance().get(id);
		if (gt == null) {
			return null;
		}
		return gt.getCronExpression();
	}

	@Override
	public Mapx<String, String> getConfigEnableTasks() {
		return null;
	}

	@Override
	public boolean enable4Front() {
		return true;
	}

}
