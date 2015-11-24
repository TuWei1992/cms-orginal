package com.zving.platform.ui;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DBConnPoolManager;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.point.BeforeLocalUpdate;
import com.zving.platform.privilege.SystemInfoPriv;
import com.zving.preloader.PreClassLoader;

/**
 * @author 甘明
 * @email gm@zving.com
 * @date 2013-11-4
 */
@Alias("LocalUpdate")
public class LocalUpdateUI extends UIFacade {

	@Priv(SystemInfoPriv.Update)
	public void update() {
		ExtendManager.invoke(BeforeLocalUpdate.ExtendPointID, new Object[] { Current.getTransaction() });
		LongTimeTask ltt = new LongTimeTask() {
			@Override
			public void execute() {
				setCurrentInfo("开始升级数据库...");
				File file = new File(Config.getContextRealPath() + "WEB-INF/data/updater/");
				File[] list = null;
				if (!file.isDirectory() || (list = file.listFiles()).length == 0) {
					fail(Lang.get("Platform.NotNeedUpdate"));
					return;
				}
				int i = 0;
				for (File updater : list) {
					String fileName = updater.getName();
					int end = fileName.lastIndexOf('.');
					setPercent(new Double(++i * 100 / list.length).intValue());
					setCurrentInfo("正在升级" + fileName);
					String versionName = fileName.substring(0, end);
					if (fileName.endsWith(".jar")) {
						PreClassLoader loader = PreClassLoader.getInstance();
						try {
							loader.addUrl(updater.toURI().toURL());
							InputStream is = loader.getResourceAsStream(DBConnPoolManager.getDBConnConfig().DBType + "_" + versionName
									+ ".sql");
							if (is != null) {
								Transaction tran = new Transaction();
								executeSQL(is, tran);
								if (tran.commit()) {
									LogUtil.info("更新" + fileName + " " + Lang.get("Common.ExecuteSuccess"));
								} else {
									addError("更新" + fileName + "/" + DBConnPoolManager.getDBConnConfig().DBType + "_" + versionName
											+ ".sql" + Lang.get("Common.ExecuteFailed"));
									LogUtil.error("更新" + fileName + " " + Lang.get("Common.ExecuteFailed") + tran.getExceptionMessage());
								}
							} else {
								LogUtil.info("更新" + fileName + "沒有可运行的SQL文件.");
							}
							Class<?> c = loader.loadClass(versionName);
							Method m = c.getMethod("main", String[].class);
							m.invoke(null, new Object[] { new String[] {} });
						} catch (ClassNotFoundException e) {
							LogUtil.info("更新" + fileName + "沒有可运行的Java文件.");
							continue;
						} catch (Exception e) {
							LogUtil.info("更新" + fileName + Lang.get("Common.ExecuteFailed") + e.getMessage());
							addError("更新" + fileName + "/" + versionName + ".java" + Lang.get("Common.ExecuteFailed"));
							continue;
						}
					}
				}
				setPercent(100);
			}
		};
		ltt.setUser(User.getCurrent());
		ltt.start();
		$S("TaskID", "" + ltt.getTaskID());
	}

	private void executeSQL(InputStream is, Transaction tran) {
		String sqlFile = FileUtil.readText(is, "UTF-8");
		Pattern p = Pattern.compile("(?ms)('(?:''|[^'])*')|--.*?$|/\\*.*?\\*/");
		sqlFile = p.matcher(sqlFile).replaceAll("$1");
		String[] sqls = sqlFile.split(";");
		int flag = 0;
		String tmpStr = null;
		for (String sql : sqls) {
			flag = pair('\"', 2, flag, sql);
			flag = pair('\'', 1, flag, sql);
			if (tmpStr != null) {
				sql = tmpStr + ';' + sql;
			}
			if (flag == 0) {
				if (StringUtil.isNotEmpty(sql.trim())) {
					QueryBuilder qb = new QueryBuilder(sql);
					tran.add(qb);
				}
				tmpStr = null;
			} else {
				tmpStr = sql;
			}
		}
	}

	private int pair(char p, int pi, int flag, String sql) {
		int length = sql.length();
		int i = 0;
		while ((i = sql.indexOf(p, i + 1)) >= 0 && i < length) {
			flag = flag ^ pi;
		}
		return flag;
	}
}
