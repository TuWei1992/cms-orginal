package com.zving.platform.util;

import java.io.File;
import java.io.FilenameFilter;

import com.zving.framework.Config;
import com.zving.framework.orm.DBExporter;
import com.zving.framework.schedule.SystemTask;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;

public class DBBackupTask extends SystemTask {
	public final static String ID = "com.zving.platform.service.DBBackupTask";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.DBBackupTask}";
	}

	@Override
	public void execute() {
		long start = System.currentTimeMillis();

		String backupDir = Config.getContextRealPath() + "WEB-INF/backup/";
		File[] files = new File(backupDir).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".zdt")) {
					return true;
				}
				return false;
			}
		});
		if (files != null) {
			for (File file : files) {
				// 删除超过30天的备份文件
				if (start - file.lastModified() > 1000L * 60 * 60 * 24 * 30) {
					FileUtil.delete(file);
				}
			}
		}
		String fileName = "DataBackupTask_" + DateUtil.getCurrentDate("yyyyMMddHHmmss") + ".zdt";
		DBExporter dbExporter = new DBExporter();
		dbExporter.exportDB(backupDir + fileName);

		LogUtil.info("定时备份数据库完成，耗时(s)：" + (System.currentTimeMillis() - start) / 1000 + " 。");
	}

	public static void main(String[] args) {
	}

	@Override
	public String getDefaultCronExpression() {
		return "0 4 * * *";
	}
}
