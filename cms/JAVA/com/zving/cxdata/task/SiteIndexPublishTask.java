package com.zving.cxdata.task;

import java.util.Date;

import com.zving.contentcore.bl.PublishBL;
import com.zving.cxdata.bl.CXPublishBL;
import com.zving.cxdata.property.CXDataCronPublishEnable;
import com.zving.cxdata.property.CXDataCronPublishPeriod;
import com.zving.cxdata.property.CXDataCronPublishPeriodType;
import com.zving.cxdata.property.CXDataCronPublishStartTime;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.schedule.CronMonitor;
import com.zving.framework.schedule.SystemTask;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.ui.ScheduleUI;
import com.zving.schema.ZCSite;

public class SiteIndexPublishTask extends SystemTask {
	public static final String ID = "SiteIndexPublishTask";
	public static final String NAME = "站点首页定时发布任务 ";

	@Override
	public String getExtendItemID() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public String getExtendItemName() {
		// TODO Auto-generated method stub
		return NAME;
	}

	@Override
	public void execute() {
		 DAOSet<ZCSite> set = new ZCSite().query();
		    if ((!ObjectUtil.empty(set)) && (set.size() > 0)) {
		      for (ZCSite schema : set)
		      {
		        String propers = schema.getConfigProps();
		        if (StringUtil.isNotNull(propers))
		        {
		          Date startTime = CXDataCronPublishStartTime.getValue(propers);
		          
		          int period = CXDataCronPublishPeriod.getValue(propers);
		          
		          String periodType = CXDataCronPublishPeriodType.getValue(propers);
		          
		          String isUsing = CXDataCronPublishEnable.getValue(propers) ? "Y" : "N";
		          if ("Y".equals(isUsing))
		          {
		            Date current = new Date();
		            if (DateUtil.compare(DateUtil.toDateTimeString(startTime), DateUtil.toDateTimeString(current), "yyyy-MM-dd HH:mm:ss") < 0)
		            {
		              String cron = ScheduleUI.getCronExpression(periodType, period, startTime);
		              
		              current.setSeconds(startTime.getSeconds());
		              if (CronMonitor.isOnTime(current, cron))
		              {
		                LogUtil.info("站点首页定时发布，站点：" + schema.getName());
		                PublishBL.publishSiteIndex(schema);
		                CXPublishBL.publishSiteIndex(schema, null);
		              }
		            }
		          }
		        }
		      }
		    }
	}

	@Override
	public String getDefaultCronExpression() {
		// TODO Auto-generated method stub
		return "* * * * *";
	}

}
