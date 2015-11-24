package com.zving.platform.util;

import com.zving.framework.collection.Mapx;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.schedule.AbstractTaskManager;
import com.zving.schema.ZDSchedule;

/**
 * 可通过界面配置的任务管理器，会将任务执行计划保存在数据库ZDSchedule表中
 * 
 * @Author 王育春
 * @Date 2009-4-16
 * @Mail wyuch@zving.com
 */
public abstract class ConfigEnableTaskManager extends AbstractTaskManager {
	protected static DAOSet<ZDSchedule> set;
	protected static long lastTime;

	@Override
	public Mapx<String, String> getUsableTasks() {
		querySchedule();
		Mapx<String, String> map = new Mapx<String, String>();
		for (int i = 0; i < set.size(); i++) {
			ZDSchedule s = set.get(i);
			if (s.getTypeCode().equals(getExtendItemID()) && "Y".equals(s.getIsUsing())) {
				map.put(s.getSourceID() + "", "");
			}
		}
		return map;
	}

	private synchronized void querySchedule() {
		if (set == null || System.currentTimeMillis() - lastTime > 60000) {
			set = new ZDSchedule().query();
			lastTime = System.currentTimeMillis();
		}
	}

	@Override
	public String getTaskCronExpression(String id) {
		querySchedule();
		for (int i = 0; i < set.size(); i++) {
			ZDSchedule s = set.get(i);
			if (s.getTypeCode().equals(getExtendItemID()) && id.equals(s.getSourceID() + "")) {
				return s.getCronExpression();
			}
		}
		return null;
	}
}
