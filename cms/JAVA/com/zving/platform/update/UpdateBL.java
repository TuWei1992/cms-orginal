package com.zving.platform.update;

import java.util.Date;

import com.zving.framework.data.DataTable;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.platform.update.UpdateServer.PluginUpdateRecord;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-8-16
 */
public class UpdateBL {
	public static DataTable getUpdatePlugins(String server) throws Exception {
		UpdateServer us = new UpdateServer(server);
		us.loadContent();
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Time");
		dt.insertColumn("Size");
		dt.insertColumn("NeedUpdate");
		for (PluginUpdateRecord upr : us.getPluginUpdateRecords()) {
			String time = DateUtil.toDateTimeString(new Date(upr.LastUpdateTime));
			String size = NumberUtil.format(upr.FileSize, "###,###,###");
			dt.insertRow(new Object[] { upr.ID, time, size, upr.NeedUpdate ? "Y" : "N" });
		}
		return dt;
	}
}
