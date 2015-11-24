package com.zving.cxdata.task;

import java.util.Date;

import com.zving.contentcore.ICatalogType;
import com.zving.contentcore.bl.ContentBL;
import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.service.CatalogTypeService;
import com.zving.cxdata.CXDataContentType;
import com.zving.cxdata.property.CXDataCronPublishEnable;
import com.zving.cxdata.property.CXDataCronPublishPeriod;
import com.zving.cxdata.property.CXDataCronPublishPeriodType;
import com.zving.cxdata.property.CXDataCronPublishStartTime;
import com.zving.framework.data.Q;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.schedule.CronMonitor;
import com.zving.framework.schedule.SystemTask;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.ui.ScheduleUI;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCSite;

/**
 * 车享数据页面定时发布任务
 * @author v_zhouquan
 *
 */
public class CXDataPublishTask extends SystemTask {
	public static final String ID = "CXDataPublishTask";
	public static final String NAME = "车享数据定时发布任务";
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
	      for (ZCSite dao : set)
	      {
	        ZCCatalog catalog = new ZCCatalog();
	        DAOSet<ZCCatalog> catalogSet = catalog.query(new Q("where SiteID=? and ContentType=?", new Object[] { Long.valueOf(dao.getID()), CXDataContentType.ID }));
	        if ((!ObjectUtil.empty(catalogSet)) && (catalogSet.size() > 0)) {
	          for (ZCCatalog catalogDAO : catalogSet) {
	            String propers = catalogDAO.getConfigProps();
	            if (StringUtil.isNotNull(propers)) {
	              Date startTime = CXDataCronPublishStartTime.getValue(propers);
	              
	              int period = CXDataCronPublishPeriod.getValue(propers);
	              
	              String periodType = CXDataCronPublishPeriodType.getValue(propers);
	              
	              String isUsing = CXDataCronPublishEnable.getValue(propers) ? "Y" : "N";
	              if ("Y".equals(isUsing))
	              {
	                Date current = new Date();
	                if (DateUtil.compare(DateUtil.toDateTimeString(startTime), DateUtil.toDateTimeString(current), 
	                  "yyyy-MM-dd HH:mm:ss") < 0)
	                {
	                  String cron = ScheduleUI.getCronExpression(periodType, period, startTime);
	                  
	                  current.setSeconds(startTime.getSeconds());
	                  if (CronMonitor.isOnTime(current, cron))
	                  {
	                    LogUtil.info("定时发布车享数据栏目，栏目：" + catalogDAO.getName());

	                    ICatalogType catalogType = (ICatalogType)CatalogTypeService.getInstance().get(catalogDAO.getType());
	                    catalogType.publish(catalogDAO, false);
	                  }
	                }
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
