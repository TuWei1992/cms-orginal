package com.zving.platform.log.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Date;

import com.zving.framework.Config;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.ILogMenu;
import com.zving.platform.ILogMenuGroup;
import com.zving.platform.ILogType;
import com.zving.platform.bl.LogBL;
import com.zving.platform.log.type.CronLog;
import com.zving.platform.log.type.SQLLog;
import com.zving.platform.log.type.SecurityLog;
import com.zving.platform.log.type.UserLog;
import com.zving.platform.service.LogMenuGroupService;
import com.zving.platform.service.LogMenuService;
import com.zving.platform.service.LogTypeService;
import com.zving.schema.ZDUserLog;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-11
 */
@Alias("Log")
public class LogUI extends UIFacade {

	@Priv
	public void bindTree(TreeAction ta) {
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("ParentID");
		dt.insertColumn("Name");
		dt.insertColumn("Target");

		for (ILogMenuGroup group : LogMenuGroupService.getInstance().getAll()) {
			dt.insertRow(new Object[] { group.getExtendItemID(), "", group.getExtendItemName(), "" });
		}
		for (ILogMenu type : LogMenuService.getInstance().getAll()) {
			dt.insertRow(new Object[] { type.getExtendItemID(), type.getGroupID(), type.getExtendItemName(), type.getDetailURL() });
		}
		dt.insertColumn("Icon");
		for (DataRow dr : dt) {
			if (ObjectUtil.empty(dr.get("Target"))) {
				dr.set("Icon", "icons/extra/icon_folder_other.gif");
			} else {
				dr.set("Icon", "icons/icon026a1.png");
			}
		}
		ta.setRootText(Lang.get("Logs.LogViewer"));
		ta.bindData(dt);
	}

	@Priv
	public void bindSQLGrid(DataGridAction dga) {
		LogBL.bindGridAction(dga, SQLLog.ID);
	}

	@Priv
	public void bindCronGrid(DataGridAction dga) {
		LogBL.bindGridAction(dga, CronLog.ID);
	}

	@Priv
	public void bindDownLogGrid(DataGridAction dga) {
		DataTable dt = new DataTable();
		dt.insertColumns(new String[] { "FileName", "FileSize", "LastModifyTime" });

		File logsDir = new File(Config.getContextRealPath() + "WEB-INF/logs/");
		FileUtil.mkdir(logsDir.getAbsolutePath());
		final String fileName = $V("FileName");
		final String startDate = $V("StartDate");
		final String endDate = $V("EndDate");

		File[] files = logsDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				String name = file.getName();
				if (name.endsWith(".svn") || name.endsWith(".temp") || name.endsWith(".deploy") || name.endsWith(".deploytemp")) {
					return false;
				}
				if (StringUtil.isNotNull(fileName)) {
					if (name.indexOf(fileName) < 0) {
						return false;
					}
				}
				if (DateUtil.isDateTime(startDate)) {
					if (file.lastModified() < DateUtil.parseDateTime(startDate).getTime()) {
						return false;
					}
				}
				if (DateUtil.isDateTime(endDate)) {
					if (file.lastModified() > DateUtil.parseDateTime(endDate).getTime()) {
						return false;
					}
				}
				return true;
			}
		});

		files = ObjectUtil.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if (o2.lastModified() - o1.lastModified() > 0) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		int i = 0;
		int start = dga.getPageIndex() * dga.getPageSize();
		int end = (dga.getPageIndex() + 1) * dga.getPageSize();
		dga.setTotal(files.length);

		for (File f : files) {
			i++;
			if (i <= start) {
				continue;
			}
			if (i > end) {
				break;
			}

			Date d = new Date(f.lastModified());
			long length = f.length() / 1024;
			if (f.length() % 1024 != 0) {
				length++;
			}
			DecimalFormat df = new DecimalFormat("#,###");
			String size = df.format(length) + " KB";
			dt.insertRow(new Object[] { f.getName(), size, DateUtil.toDateTimeString(d) });
		}

		dga.bindData(dt);
	}

	@Priv
	public void bindSecurityGrid(DataGridAction dga) {
		LogBL.bindGridAction(dga, SecurityLog.ID);
	}

	@Priv
	public void bindUserGrid(DataGridAction dga) {
		LogBL.bindGridAction(dga, UserLog.ID);
	}

	@Priv
	public void bindUserLoginGrid(DataGridAction dga) {
		dga.getParams().put("SubType", UserLog.SubType_Login);
		LogBL.bindGridAction(dga, UserLog.ID);
	}

	@Priv
	public DataTable getSubTypeData() {
		String typeID = $V("TypeID");
		ILogType type = LogTypeService.getInstance().get(typeID);
		if (type == null) {
			return new DataTable();
		}
		return type.getSubTypes().toDataTable();
	}

	@Priv
	public void initSQLLog() {
		String username = $V("UserName");
		long logID = $L("LogID");
		if (ObjectUtil.notEmpty(username) && logID != 0) {
			ZDUserLog uLog = new ZDUserLog();
			uLog.setUserName(username.trim());
			uLog.setLogID(logID);
			if (uLog.fill()) {
				$S("UserName", uLog.getUserName());
				$S("LogID", uLog.getLogID());
				$S("AddTime", uLog.getAddTime());
				$S("IP", uLog.getIP());
				$S("LogMessage", uLog.getLogMessage());
			}
		}
	}

	@Priv
	public void initErrorLog() {
		String username = $V("UserName");
		long logID = $L("LogID");
		if (ObjectUtil.notEmpty(username) && logID != 0) {
			ZDUserLog uLog = new ZDUserLog();
			uLog.setUserName(username.trim());
			uLog.setLogID(logID);
			if (uLog.fill()) {
				$S("UserName", uLog.getUserName());
				$S("LogID", uLog.getLogID());
				$S("AddTime", uLog.getAddTime());
				$S("LogMessage", uLog.getLogMessage());
			}
		}
	}

	@Priv
	@Alias(value = "log/errorlog/download", alone = true)
	public void download(ZAction za) {
		String fileName = $V("FileName");
		String filePath = Config.getContextRealPath() + "WEB-INF/logs/" + fileName;
		filePath = FileUtil.normalizePath(filePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filePath);
			IOUtil.download(za.getRequest(), za.getResponse(), fileName, fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			za.writeHTML("File not found: " + fileName);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
