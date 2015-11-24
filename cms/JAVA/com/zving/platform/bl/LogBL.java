package com.zving.platform.bl;

import java.util.Date;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.User;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.ILogType;
import com.zving.platform.service.LogTypeService;
import com.zving.platform.util.NoUtil;
import com.zving.schema.ZDUserLog;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-4-6
 */
public class LogBL {
	private static DAOSet<ZDUserLog> set = new DAOSet<ZDUserLog>();
	private static long lastTime = 0;
	private static Object mutex = new Object();

	/**
	 * 添加指定类型和子类型的日志。
	 */
	public static void addLog(String type, String subType, String message) {
		if (!Config.isInstalled()) {
			return;
		}
		ZDUserLog log = new ZDUserLog();
		log.setUserName(User.getUserName() == null ? "-" : User.getUserName());
		log.setIP(Current.getRequest() == null ? "-" : Current.getRequest().getClientIP());
		log.setLogMessage(message);
		log.setLogType(type);
		log.setSubType(subType);
		log.setAddTime(new Date());
		synchronized (mutex) {
			set.add(log);
			long thisTime = System.currentTimeMillis();
			if (set.size() > 100 || thisTime - lastTime > 180000) {// 每3分钟写入数据库
				lastTime = thisTime;
				final DAOSet<ZDUserLog> oldSet = set;
				set = new DAOSet<ZDUserLog>();
				new Thread() {
					@Override
					public void run() {
						for (ZDUserLog log : oldSet) {
							log.setLogID(NoUtil.getMaxID("ZDUserLogID"));
						}
						oldSet.insert();
					}
				}.start();
			}
		}
	}

	/**
	 * 绑定指定类型的日志信息
	 */
	public static void bindGridAction(DataGridAction dga, String logType) {
		Q q = new Q().select("*").from("ZDUserLog").where("LogType", logType);
		if (ObjectUtil.notEmpty(dga.getParam("SubType"))) {
			q.and().eq("SubType", dga.getParam("SubType"));
		}
		if (ObjectUtil.notEmpty(dga.getParam("UserName"))) {
			q.and().like("UserName", dga.getParam("UserName"));
		}
		if (ObjectUtil.notEmpty(dga.getParam("IP"))) {
			q.and().like("IP", dga.getParam("IP"));
		}
		if (ObjectUtil.notEmpty(dga.getParam("Message"))) {
			q.and().like("LogMessage", dga.getParam("Message"));
		}
		String startDate = dga.getParams().getString("StartDate");
		String endDate = dga.getParams().getString("EndDate");

		if (StringUtil.isNotEmpty(startDate) && StringUtil.isNotEmpty(endDate)) {
			q.and().ge("AddTime", DateUtil.parse(startDate, "yyyy-MM-dd HH:mm:ss"));
			q.and().le("AddTime", DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss"));
		}
		q.orderby("AddTime desc"); // 根据记录创建时间排序

		ILogType type = LogTypeService.getInstance().get(logType);
		DataTable dt = q.fetch(dga.getPageSize(), dga.getPageIndex());
		type.decodeMessage(dt);
		dga.setTotal(q);
		dga.bindData(dt);
	}
}
