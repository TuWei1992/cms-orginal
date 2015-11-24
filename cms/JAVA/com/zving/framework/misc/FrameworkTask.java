package com.zving.framework.misc;

import java.io.File;

import com.zving.framework.Config;
import com.zving.framework.schedule.SystemTask;
import com.zving.framework.utility.FileUtil;

/**
 * 定时清空Debug模式下的Session缓存
 * 
 * @Author 王育春
 * @Date 2008-12-27
 * @Mail wyuch@zving.com
 */
public class FrameworkTask extends SystemTask {
	public static final String ID = "com.zving.framwork.FrameworkTask";

	@Override
	public void execute() {
		// 清除缓存
		if (!Config.isDebugMode()) {
			return;
		}
		File dir = new File(Config.getContextRealPath() + "WEB-INF/cache/");
		File[] fs = dir.listFiles();
		for (File f : fs) {
			if (f.isFile()) {
				FileUtil.delete(f);
			}
		}
	}

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.FrameworkTask}";
	}

	@Override
	public String getDefaultCronExpression() {
		return "30 10,16 * * *";
	}

	@Override
	public boolean enable4Front() {
		return true;
	}
}
