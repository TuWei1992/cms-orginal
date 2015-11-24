package com.zving.framework.schedule;

import java.util.Timer;

import com.zving.framework.Config;
import com.zving.framework.collection.Mapx;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.utility.LogUtil;

/**
 * 定时器管理器，以指定的间隔调用CronMontior中的run()
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2005-8-23
 */
public class CronManager {
	public static final long SCAN_INTERVAL = 1000;

	private Timer timer; // 仅用来定期调用CronMontior.run()的计时器

	private static CronManager instance = new CronManager();

	private CronManager() {
		if (!Config.isInstalled()) {
			return;
		}
		timer = new Timer("Cron Manager Timer", true);
		long millis = System.currentTimeMillis() % 1000;
		timer.schedule(CronMonitor.getInstance(), millis, SCAN_INTERVAL);// 准时开始
		LogUtil.info("----" + Config.getAppCode() + "(" + LangUtil.get(Config.getAppName()) + "): CronManager initialized----");
	}

	public static CronManager getInstance() {
		return instance;
	}

	/**
	 * 本方法用于后台界面显示
	 */
	public Mapx<String, String> getTaskTypes() {
		Mapx<String, String> map = new Mapx<String, String>();
		for (AbstractTaskManager ctm : CronTaskManagerService.getInstance().getAll()) {
			if (ctm instanceof SystemTaskManager) {
				continue;
			}
			map.put(ctm.getExtendItemID(), ctm.getExtendItemName());
		}
		return map;
	}

	/**
	 * 本方法用于后台界面显示
	 */
	public Mapx<String, String> getConfigEnableTasks(String id) {
		AbstractTaskManager ctm = CronTaskManagerService.getInstance().get(id);
		if (ctm == null) {
			return null;
		}
		return ctm.getConfigEnableTasks();
	}

	/**
	 * 销毁定时管理器
	 */
	public void destory() {
		CronMonitor.getInstance().destory();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}
