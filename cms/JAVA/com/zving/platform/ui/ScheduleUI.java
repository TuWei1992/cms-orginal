package com.zving.platform.ui;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataColumn;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.Lang;
import com.zving.framework.schedule.CronManager;
import com.zving.framework.schedule.CronMonitor;
import com.zving.framework.schedule.SystemTask;
import com.zving.framework.schedule.SystemTaskManager;
import com.zving.framework.schedule.SystemTaskService;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.framework.xml.XMLDocument;
import com.zving.framework.xml.XMLElement;
import com.zving.framework.xml.XMLParser;
import com.zving.framework.xml.XMLWriter;
import com.zving.platform.code.Enable;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.privilege.SchedulePriv;
import com.zving.platform.util.NoUtil;
import com.zving.schema.ZDSchedule;

/**
 * @Author 兰军
 * @Date 2008-3-14
 * @Mail lanjun@zving.com
 */
@Alias("Schedule")
public class ScheduleUI extends UIFacade {
	@Priv(SchedulePriv.MenuID)
	public void init() {
		$S("IsUsing", Enable.Enable);
		String id = $V("ID");
		if (ObjectUtil.empty(id)) {
			return;
		}
		if (NumberUtil.isLong(id)) {
			ZDSchedule schedule = new ZDSchedule();
			schedule.setID(id);
			if (!schedule.fill()) {
				return;
			}
			Response.putAll(schedule.toMapx());
			if (ObjectUtil.notEmpty(schedule.getStartTime())) {
				$S("StartTime", DateUtil.toDateTimeString(schedule.getStartTime()));
			}
			if ("Period".equals(schedule.getPlanType())) {
				if (schedule.getCronExpression().endsWith(" * * * *")) {
					$S("PeriodType", "Minute");
				} else if (schedule.getCronExpression().endsWith(" * * *")) {
					$S("PeriodType", "Hour");
				} else if (schedule.getCronExpression().endsWith(" * *")) {
					$S("PeriodType", "Day");
				} else if (schedule.getCronExpression().endsWith(" *")) {
					$S("PeriodType", "Month");
				}
			}
		} else {// 看是否是系统任务
			SystemTask task = SystemTaskService.getInstance().get(id);
			if (task != null) {
				$S("ID", task.getExtendItemID());
				$S("Name", task.getExtendItemName());
				$S("TypeCode", SystemTaskManager.ID);
				$S("TypeCodeName", "@{Framework.SystemTask}");
				$S("Description", task.getExtendItemID());
				$S("PlanType", "Cron");
				$S("IsUsing", task.isDisabled() ? Enable.Disable : Enable.Enable);
				$S("StartTime", "1970-01-01 00:00:00");
				$S("CronExpression", task.getCronExpression());
			}
		}
	}

	@Priv(SchedulePriv.MenuID)
	public DataTable getTypes() {
		Mapx<String, String> params = CronManager.getInstance().getTaskTypes();
		return params.toDataTable();
	}

	@Priv(SchedulePriv.MenuID)
	public DataTable getUsableTask() {
		String type = $V("TypeCode");
		if (ObjectUtil.empty(type)) {
			return new DataTable();
		}
		Map<String, String> map = CronManager.getInstance().getConfigEnableTasks(type);
		if (map == null) {
			return new DataTable();
		}
		return Mapx.toDataTable(map);
	}

	@Priv(SchedulePriv.MenuID)
	public void dg1DataBind(DataGridAction dga) {
		Q q = new Q().select("*").from("ZDSchedule").append(dga.getSortString());
		if (ObjectUtil.empty(dga.getSortString())) {
			q.orderby("ID desc");
		}
		DataTable dt = q.fetch();
		dt.getDataColumn("ID").setColumnType(DataTypes.STRING);
		dt.getDataColumn("SourceID").setColumnType(DataTypes.STRING);

		for (SystemTask task : SystemTaskService.getInstance().getAll()) {
			dt.insertRow((Object[]) null);
			DataRow dr = dt.getDataRow(dt.getRowCount() - 1);
			dr.set("ID", task.getExtendItemID());
			dr.set("TypeCode", SystemTaskManager.ID);
			dr.set("SourceID", task.getExtendItemID());
			dr.set("isUsing", task.isDisabled() ? Enable.Disable : Enable.Enable);
			dr.set("StartTime", new Date());
			dr.set("CronExpression", task.getCronExpression());
			dr.set("Description", task.getExtendItemID());
		}
		Mapx<String, String> map = CronManager.getInstance().getTaskTypes();
		dt.decodeColumn("TypeCode", map);

		dt.insertColumn("SourceIDName");
		dt.insertColumn(new DataColumn("NextRunTime", DataTypes.DATETIME));
		for (int i = dt.getRowCount() - 1; i >= 0; i--) {
			DataRow dr = dt.getDataRow(i);
			String typeCode = dr.getString("TypeCode");
			String sourceID = dr.getString("SourceID");

			// 防止空指针问题 如果数据库中存在记录，但framework.xml中注释了该任务，会出现空指针
			if (SystemTaskManager.ID.equals(typeCode)) {
				dr.set("SourceIDName", SystemTaskService.getInstance().get(dr.getString("Description")).getExtendItemName());
				dr.set("TypeCodeName", "@{Framework.SystemTask}");
			} else {
				Map<String, String> taskMap = CronManager.getInstance().getConfigEnableTasks(typeCode);
				if (taskMap == null) {
					dt.deleteRow(i);
					continue;
				}
				String sourceIDName = taskMap.get(sourceID);
				if (StringUtil.isEmpty(sourceIDName)) {// 说明不是当前用户的任务
					dt.deleteRow(i);
					continue;
				}
				dt.set(i, "SourceIDName", sourceIDName);
			}
			if (Enable.isEnable(dr.getString("isUsing"))) {// 只有启用的才有下一次启动时间
				Date nextRunTime = null;
				try {
					Date st = dr.getDate("StartTime");
					Date now = new Date();
					if (!now.before(st)) {
						st = now;
						nextRunTime = CronMonitor.getNextRunTime(st, dr.getString("CronExpression"));
					} else {
						nextRunTime = st;
					}
				} catch (Exception e) {
					nextRunTime = DateUtil.parse("2999-01-01");
				}
				dr.set("NextRunTime", nextRunTime);
			}
		}
		YesOrNo.decodeYesOrNoIcon(dt, "isUsing", false);
		dga.bindData(dt);
	}

	@Priv(SchedulePriv.Add + "||" + SchedulePriv.Edit)
	public void save() {
		String id = $V("ID");
		String typeCode = $V("TypeCode");
		String startTime = $V("StartTime");
		String cronExpression = $V("CronExpression");
		if ($V("PlanType").equals("Period")) {
			cronExpression = getCronExpression($V("PeriodType"), Integer.valueOf($V("Period")), DateUtil.parseDateTime(startTime));
		}
		try {
			CronMonitor.getNextRunTime(cronExpression);
		} catch (Exception e) {
			fail(Lang.get("Common.SaveFailed") + "\nInvalid cron expression!");
		}
		if (!SystemTaskManager.ID.equals(typeCode)) {
			ZDSchedule schedule = new ZDSchedule();
			if (StringUtil.isEmpty(id)) {
				schedule.setID(NoUtil.getMaxID("ScheduleID"));
				schedule.setAddTime(new Date());
				schedule.setAddUser(User.getUserName());
			} else {
				schedule.setID(Long.parseLong(id));
				schedule.fill();
				schedule.setModifyTime(new Date());
				schedule.setModifyUser(User.getUserName());
			}

			Request.put("CronExpression", cronExpression);
			schedule.setValue(Request);
			try {
				boolean flag = StringUtil.isEmpty(id) ? schedule.insert() : schedule.update();
				if (!flag) {
					fail(Lang.get("Common.ExecuteFailed"));
				} else {
					success(Lang.get("Common.ExecuteSuccess"));
				}
			} catch (Exception e) {
				e.printStackTrace();
				fail(Lang.get("Common.SaveFailed") + "\n" + Lang.get("Cron.InvalidExpr"));
			}
		} else {
			SystemTask task = SystemTaskService.getInstance().get(id);
			if (task == null) {
				fail(Lang.get("Common.SaveFailed") + "\nTask not found:" + id);
				return;
			}
			File f = new File(Config.getClassesPath() + "framework.xml");
			if (f.exists()) {
				String xml = FileUtil.readText(f);
				XMLParser p = new XMLParser(xml);
				p.parse();
				XMLDocument doc = p.getDocument();
				List<XMLElement> eles = doc.elements("*.cron.task");
				boolean flag = false;
				for (XMLElement ele : eles) {
					if (id.equals(ele.attributeValue("id"))) {
						ele.attributes().clear();
						ele.addAttribute("id", id);
						ele.addAttribute("time", cronExpression);
						if (Enable.isDisable($V("IsUsing"))) {
							ele.addAttribute("disabled", "true");
						}
						flag = true;
						break;
					}
				}
				if (!flag) {
					if (Enable.isDisable($V("IsUsing")) != task.isDisabled() || !cronExpression.equals(task.getDefaultCronExpression())) {
						XMLElement ele = doc.addElement("framework.cron", "task");
						ele.attributes().clear();
						ele.addAttribute("id", id);
						ele.addAttribute("time", cronExpression);
						if (Enable.isDisable($V("IsUsing"))) {
							ele.addAttribute("disabled", "true");
						}
					}
				}
				XMLWriter.writeTo(doc, f);
				File javaFile = new File(Config.getWEBINFPath());
				if (javaFile.getParentFile() != null && javaFile.getParentFile().getParentFile() != null) {
					javaFile = javaFile.getParentFile().getParentFile();
					javaFile = new File(javaFile.getAbsolutePath() + "/JAVA/framework.xml");
					if (javaFile.exists()) {
						XMLWriter.writeTo(doc, javaFile);// 如果有JAVA目录，则也进行修改
					}
				}
				task.setCronExpression(cronExpression);
				task.setDisabled(Enable.isDisable($V("IsUsing")));
				success(Lang.get("Common.ExecuteSuccess"));
			} else {
				fail(Lang.get("Common.SaveFailed") + "\nframework.xml not found!");
			}
		}
	}

	@Priv(SchedulePriv.Delete)
	public void del() {
		String[] ids = StringUtil.splitEx($V("IDs"), ",");
		for (String id : ids) {
			if (!NumberUtil.isLong(id)) {
				continue;
			}
			ZDSchedule schedule = new ZDSchedule();
			schedule.setID(id);
			if (schedule.fill()) {
				Current.getTransaction().deleteAndBackup(schedule);
			}
		}
		if (!Current.getTransaction().commit()) {
			fail(Lang.get("Common.DeleteFailed"));
		} else {
			success(Lang.get("Common.DeleteSuccess"));
		}
	}

	@Priv(SchedulePriv.ManualExecute)
	public void execute() {
		String id = $V("ID");
		String typeCode = $V("TypeCode");
		if (!SystemTaskManager.ID.equals(typeCode)) {
			ZDSchedule schedule = new ZDSchedule();
			schedule.setID(id);
			if (!schedule.fill()) {
				fail(Lang.get("Cron.TaskNotFound"));
				return;
			}
			if (!Enable.isEnable(schedule.getIsUsing())) {
				fail(Lang.get("Cron.DisabledTaskCannotBeExecute"));
				return;
			}
			if (CronMonitor.getInstance().isTaskRunning(typeCode, schedule.getSourceID() + "")) {
				success("<font color=red>" + Lang.get("Cron.RunningNow") + "</font>");
			} else {
				CronMonitor.getInstance().executeTask(typeCode, schedule.getSourceID() + "");
				success(Lang.get("Cron.ManualSuccess"));
			}
		} else {
			SystemTask st = SystemTaskService.getInstance().get(id);
			if (st.isDisabled()) {
				fail(Lang.get("Cron.DisabledTaskCannotBeExecute"));
				return;
			}
			if (CronMonitor.getInstance().isTaskRunning(typeCode, id)) {
				success("<font color=red>" + Lang.get("Cron.RunningNow") + "</font>");
			} else {
				CronMonitor.getInstance().executeTask(typeCode, id);
				success(Lang.get("Cron.ManualSuccess"));
			}

		}
	}

	public static String getCronExpression(String periodType, int period, Date startTime) {
		Calendar c = Calendar.getInstance();
		c.setTime(startTime);
		StringBuilder sb = new StringBuilder();
		if ("Minute".equals(periodType)) {
			int second = c.get(Calendar.SECOND);
			sb.append(second).append(" ");
			int minute = c.get(Calendar.MINUTE);
			sb.append(minute);
			sb.append("-");
			if (minute == 0) {
				sb.append("59");
			} else {
				sb.append(minute - 1);
			}
			sb.append("/");
			sb.append(period);
			sb.append(" * * * *");
		} else if ("Hour".equals(periodType)) {
			int second = c.get(Calendar.SECOND);
			sb.append(second).append(" ");
			int minute = c.get(Calendar.MINUTE);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			sb.append(minute);
			sb.append(" ");
			sb.append(hour);
			sb.append("-");
			if (hour == 0) {
				sb.append("23");
			} else {
				sb.append(hour - 1);
			}
			sb.append("/");
			sb.append(period);
			sb.append(" * * *");
		} else if ("Day".equals(periodType)) {
			int second = c.get(Calendar.SECOND);
			int minute = c.get(Calendar.MINUTE);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int day = c.get(Calendar.DAY_OF_MONTH);
			sb.append(second);
			sb.append(" ");
			sb.append(minute);
			sb.append(" ");
			sb.append(hour);
			sb.append(" ");
			sb.append(day);
			sb.append("-");
			if (day == 1) {
				sb.append(c.getActualMaximum(Calendar.DAY_OF_MONTH));
			} else {
				sb.append(day - 1);
			}
			sb.append("/");
			sb.append(period);
			sb.append(" * *");
		} else if ("Month".equals(periodType)) {
			int second = c.get(Calendar.SECOND);
			int minute = c.get(Calendar.MINUTE);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int day = c.get(Calendar.DAY_OF_MONTH);
			int month = c.get(Calendar.MONTH) + 1;
			sb.append(second);
			sb.append(" ");
			sb.append(minute);
			sb.append(" ");
			sb.append(hour);
			sb.append(" ");
			sb.append(day);
			sb.append(" ");
			if (month == 1) {
				sb.append(month + 1);
				sb.append("-");
				sb.append(month);
			} else {
				sb.append(month);
				sb.append("-");
				sb.append(month - 1);
			}
			sb.append("/");
			sb.append(period);
			sb.append(" *");
		}
		return sb.toString();
	}
}
