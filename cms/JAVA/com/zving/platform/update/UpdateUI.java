package com.zving.platform.update;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Filter;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.config.UpdateServerURLs;
import com.zving.platform.privilege.SystemInfoPriv;
import com.zving.platform.ui.InstallUI;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-8-16
 */
@Alias("Update")
public class UpdateUI extends UIFacade {
	@Priv(SystemInfoPriv.Update)
	public void init() {
		String server = $V("Server");
		$S("NeedUpdate", true);
		try {
			DataTable dt = UpdateBL.getUpdatePlugins(server);
			boolean needUpdate = false;
			for (DataRow dr : dt) {
				if (YesOrNo.isYes(dr.getString("NeedUpdate"))) {
					needUpdate = true;
					break;
				}
			}
			dt = dt.filter(new Filter<DataRow>() {
				@Override
				public boolean filter(DataRow dr) {
					return YesOrNo.isYes(dr.getString("NeedUpdate"));
				}
			});
			Current.put("Data", dt);
			if (!needUpdate) {
				$S("NeedUpdate", false);
				$S("Message", Lang.get("Platform.NotNeedUpdate"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			$S("Message", Lang.get("Platform.UpdateServerFailed"));
			$S("NeedUpdate", false);
		}
	}

	@Priv(SystemInfoPriv.Update)
	public void bindPluginGrid(DataGridAction dga) {
		try {
			DataTable dt = (DataTable) Current.get("Data");
			dga.bindData(dt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Priv(SystemInfoPriv.Update)
	public void startUpdate() {
		final String server = $V("Server");
		try {
			$S("NeedUpdate", "Y");
			DataTable dt = UpdateBL.getUpdatePlugins(server);
			boolean needUpdate = false;
			for (DataRow dr : dt) {
				if (YesOrNo.isYes(dr.getString("NeedUpdate"))) {
					needUpdate = true;
					break;
				}
			}
			if (!needUpdate) {
				$S("NeedUpdate", "N");
				return;
			}
		} catch (Exception e) {
			$S("NeedUpdate", "N");
			e.printStackTrace();
			return;
		}
		LongTimeTask ltt = new LongTimeTask() {
			@Override
			public void execute() {
				UpdateDownloader ud = new UpdateDownloader(this, server);
				try {
					ud.download();
				} catch (Exception e1) {
					e1.printStackTrace();
					setCurrentInfo("Download failed:" + e1.getMessage());
					setPercent(0);
					return;
				}
				setPercent(100);
				InstallUI.reload();
			}
		};
		ltt.setType("Update");
		ltt.setUser(User.getCurrent());
		ltt.start();
		$S("TaskID", ltt.getTaskID());
	}

	@Priv(SystemInfoPriv.Update)
	public DataTable getServers() {
		DataTable dt = new DataTable();
		String str = UpdateServerURLs.getValue();
		dt.insertColumn("ID");
		dt.insertColumn("Name");
		if (str != null) {
			String[] arr = str.split("\\n");
			for (String server : arr) {
				server = server.trim();
				if (ObjectUtil.notEmpty(server)) {
					dt.insertRow(new Object[] { server, server });
				}
			}
		}
		return dt;
	}
}
