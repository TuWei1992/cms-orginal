package com.zving.platform.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.zving.framework.Config;
import com.zving.framework.SessionListener;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DBConnPoolManager;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DBExporter;
import com.zving.framework.orm.DBImporter;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.ui.control.UploadAction;
import com.zving.framework.ui.tag.ListAction;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.PropertiesUtil;
import com.zving.platform.bl.SystemInfoBL;
import com.zving.platform.config.AppDataPath;
import com.zving.platform.privilege.SystemInfoPriv;

/**
 * @Author 王育春
 * @Date 2007-7-13
 * @Mail wyuch@zving.com
 */
@Alias("SystemInfo")
public class SystemInfoUI extends UIFacade {

	/**
	 * 获取登陆状态
	 */
	@Priv
	public void initStatusName() {
		if (!Config.isAllowLogin()) {
			$S("StatusName", Lang.get("SysInfo.AllowLogin"));
		} else {
			$S("StatusName", Lang.get("SysInfo.DenyLogin"));
		}
		DataTable dt = getAppInfo();
		$S("AppInfo", dt);
		Response.putAll(dt.toMapx("Key", "Value"));
		dt = getLicenseInfo();
		$S("LicenseInfo", dt);
		Response.putAll(dt.toMapx("Key", "Value"));
	}

	/**
	 * 改变登陆状态
	 */
	@Priv(SystemInfoPriv.ChangeLoginStatus)
	public void changeLoginStatus() {
		Config.setAllowLogin(!Config.isAllowLogin());
		if (!Config.isAllowLogin()) {
			$S("LoginStatus", Lang.get("SysInfo.AllowLogin"));
		} else {
			$S("LoginStatus", Lang.get("SysInfo.DenyLogin"));
		}
	}

	/**
	 * 强制退出
	 */
	@Priv(SystemInfoPriv.ForceExit)
	public void forceExit() {
		SessionListener.forceExit();
		Response.setStatus(1);
	}

	/**
	 * 获取应用信息
	 */
	private DataTable getAppInfo() {
		DataTable dt = new DataTable();
		dt.insertColumn("Key");
		dt.insertColumn("Name");
		dt.insertColumn("Value");
		dt.insertRow(new Object[] { "AppCode", Lang.get("SysInfo.AppCode"), Config.getAppCode() });
		String title = Lang.get("Product.Name");
		dt.insertRow(new Object[] { "AppName", Lang.get("SysInfo.AppName"), title == null ? Config.getAppName() : title });
		dt.insertRow(new Object[] { "AppVersion", Lang.get("SysInfo.AppVersion"), SystemInfoBL.getAppVersion() });
		Map<String, String> version = PropertiesUtil.read( new File(Config.getPluginPath() + "classes/version.properties"));
		dt.insertRow(new Object[] { "cxVersion", "车享版本", version.get("version") });
		
		dt.insertRow(new Object[] { "StartupTime", Lang.get("SysInfo.StartupTime"),
				DateUtil.toString(new Date(Long.parseLong(Config.getValue("App.Uptime"))), "yyyy-MM-dd HH:mm:ss") });
		
		dt.insertRow(new Object[] { "LoginCount", Lang.get("SysInfo.LoginCount"), new Long(Config.getLoginUserCount()) });
		dt.insertRow(new Object[] { "DebugMode", Lang.get("SysInfo.DebugMode"), Config.getValue("App.DebugMode") });
		return dt;
	}

	/**
	 * 获取系统信息
	 */
	@Priv(SystemInfoPriv.MenuID)
	public void bindSystemInfo(ListAction dla) {
		DataTable dt = new DataTable();
		dt.insertColumn("Name");
		dt.insertColumn("Value");
		dt.insertRow(new Object[] { Lang.get("SysInfo.OSName"), Config.getValue("System.OSName") });
		dt.insertRow(new Object[] { Lang.get("SysInfo.OSVersion"), Config.getValue("System.OSVersion") });
		dt.insertRow(new Object[] { Lang.get("SysInfo.OSPatch"), Config.getValue("System.OSPatchLevel") });// 其他JDK以后补充
		dt.insertRow(new Object[] { Lang.get("SysInfo.JDKVendor"), Config.getValue("System.JavaVendor") });
		dt.insertRow(new Object[] { Lang.get("SysInfo.JDKVersion"), Config.getValue("System.JavaVersion") });
		dt.insertRow(new Object[] { Lang.get("SysInfo.JDKHome"), Config.getValue("System.JavaHome") });
		dt.insertRow(new Object[] { Lang.get("SysInfo.ContainerName"), Config.getValue("System.ContainerInfo") });
		dt.insertRow(new Object[] { Lang.get("SysInfo.ContainerUser"), Config.getValue("System.OSUserName") });
		dt.insertRow(new Object[] { Lang.get("SysInfo.MemoryInfo"),
				Runtime.getRuntime().totalMemory() / 1024 / 1024 + "M/" + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "M" });
		dt.insertRow(new Object[] { Lang.get("SysInfo.FileEncoding"), Config.getFileEncode() });
		dla.bindData(dt);
	}

	/**
	 * 获取数据库信息
	 */
	@Priv(SystemInfoPriv.MenuID)
	public void bindDBInfo(ListAction dla) {
		DataTable dt = new DataTable();
		dt.insertColumn("Name");
		dt.insertColumn("Value");
		DBConnConfig dcc = DBConnPoolManager.getDBConnConfig();
		dt.insertRow(new Object[] { Lang.get("SysInfo.DBType"), dcc.DBType });
		if (dcc.isJNDIPool) {
			dt.insertRow(new Object[] { Lang.get("SysInfo.JNDIName"), dcc.JNDIName });
		} else {
			dt.insertRow(new Object[] { Lang.get("SysInfo.DBAddress"), dcc.DBServerAddress });
			dt.insertRow(new Object[] { Lang.get("SysInfo.DBPort"), "" + dcc.DBPort });
			dt.insertRow(new Object[] { Lang.get("SysInfo.DBName"), dcc.DBName });
			dt.insertRow(new Object[] { Lang.get("SysInfo.DBUser"), dcc.DBUserName });
		}
		dla.bindData(dt);
	}

	/**
	 * 获取授权信息
	 */
	private DataTable getLicenseInfo() {
		DataTable dt = new DataTable();
		dt.insertColumn("Key");
		dt.insertColumn("Name");
		dt.insertColumn("Value");
		dt.insertRow(new Object[] { "LicenseTo", Lang.get("SysInfo.LicenseTo"), LicenseInfo.getName() });
		String validEndDate = DateUtil.toString(LicenseInfo.getEndDate(), "yyyy-MM-dd HH:mm:ss");
		dt.insertRow(new Object[] { "ValidEndDate", Lang.get("SysInfo.ValidEndDate"), validEndDate });
		dt.insertRow(new Object[] { "LicensedUserCount", Lang.get("SysInfo.LicensedUserCount"), new Long(LicenseInfo.getUserLimit()) });
		dt.insertRow(new Object[] { "LicensedProduct", Lang.get("SysInfo.LicensedProduct"), Config.getAppCode() });
		dt.insertRow(new Object[] { "LicensedMac", Lang.get("SysInfo.LicensedMac"), LicenseInfo.getMacAddress() });
		return dt;
	}

	/**
	 * 导出数据库
	 */
	@Priv(SystemInfoPriv.Export)
	public void exportDB() {
		LongTimeTask ltt = LongTimeTask.getInstanceByType("ExportDB");
		if (ltt != null && ltt.isAlive()) {
			fail("Platform.DBExportTaskRunning");
			return;
		}
		final String fileName = "DB_" + DateUtil.getCurrentDate("yyyyMMddHHmmss") + ".zdt";
		final String path = AppDataPath.getValue() + "backup/" + fileName;
		ltt = new LongTimeTask() {
			@Override
			public void execute() {
				long start = System.currentTimeMillis();

				DBExporter dbExporter = new DBExporter();
				dbExporter.setTask(this);
				dbExporter.exportDB(path);

				LogUtil.info("导出数据库完成，耗时(s)：" + (System.currentTimeMillis() - start) / 1000 + " 。");
			}
		};
		ltt.setType("ExportDB");
		ltt.setUser(User.getCurrent());
		ltt.start();
		$S("TaskID", ltt.getTaskID());
		$S("FileName", fileName);
	}

	@Priv(SystemInfoPriv.Export)
	@Alias(value = "platform/systeminfo/download", alone = true)
	public void downloadDB(ZAction za) {
		String fileName = $V("FileName");
		String path = AppDataPath.getValue() + "backup/" + fileName;
		path = FileUtil.normalizePath(path);
		fileName = path.substring(path.lastIndexOf("/") + 1);
		try {
			if (!FileUtil.exists(path)) {
				za.writeHTML("<script>alert('" + Lang.get("Platform.SystemInfo.DBExport.FileNotExisted") + "')</script>");
				return;
			}
			FileInputStream fis = new FileInputStream(path);
			IOUtil.download(za.getRequest(), za.getResponse(), fileName, fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 导入数据库
	 */
	@Priv(SystemInfoPriv.Import)
	public void uploadDB(UploadAction ua) {
		final String fileName = AppDataPath.getValue() + "backup/DBUpload_" + DateUtil.getCurrentDate("yyyyMMddHHmmss") + ".zdt";
		try {
			if (!FileUtil.exists(AppDataPath.getValue() + "backup")) {
				FileUtil.mkdir(AppDataPath.getValue() + "backup");
			}
			ua.getFirstFile().write(new File(fileName));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		LongTimeTask ltt = LongTimeTask.getInstanceByType("Install");
		if (ltt != null) {
			fail(Lang.get("SysInfo.Installing"));
			return;
		}
		SessionListener.forceExit();
		Config.setAllowLogin(false);
		ltt = new LongTimeTask() {
			@Override
			public void execute() {
				DBImporter di = new DBImporter();
				di.setTask(this);
				di.importDB(fileName);
				setPercent(100);
				InstallUI.reload();
			}
		};
		ltt.setType("Install");
		ltt.setUser(User.getCurrent());
		ltt.start();
		Config.setAllowLogin(true);
		$S("TaskID", ltt.getTaskID());
	}

}