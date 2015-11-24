package com.zving.contentcore.task;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.zving.block.service.BlockBL;
import com.zving.contentcore.ICatalogType;
import com.zving.contentcore.IPublishPlatform;
import com.zving.contentcore.bl.PublishBL;
import com.zving.contentcore.code.ContentStatus;
import com.zving.contentcore.property.impl.CronPublishEnable;
import com.zving.contentcore.property.impl.CronPublishPeriod;
import com.zving.contentcore.property.impl.CronPublishPeriodType;
import com.zving.contentcore.property.impl.CronPublishStartTime;
import com.zving.contentcore.service.CatalogTypeService;
import com.zving.contentcore.service.PublishPlatformService;
import com.zving.cxdata.property.CronPublishDetailEnable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.schedule.CronMonitor;
import com.zving.framework.schedule.SystemTask;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.ui.ScheduleUI;
import com.zving.schema.ZCBlock;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCSite;

public class CatalogPublishTask
  extends SystemTask
{
  public static final String ID = "com.zving.contentcore.task.CatalogPublishTask";
  
  public String getExtendItemID()
  {
    return "com.zving.contentcore.task.CatalogPublishTask";
  }
  
  public String getExtendItemName()
  {
    return "@{Platform.CatalogPublishTask}";
  }
  
  public void execute()
  {
    DAOSet<ZCSite> set = new ZCSite().query();
    if ((!ObjectUtil.empty(set)) && (set.size() > 0)) {
      for (ZCSite dao : set)
      {
        boolean publishSite = false;
        ZCCatalog catalog = new ZCCatalog();
        DAOSet<ZCCatalog> catalogSet = catalog.query(new Q("where SiteID=?", new Object[] { Long.valueOf(dao.getID()) }));
        if ((!ObjectUtil.empty(catalogSet)) && (catalogSet.size() > 0)) {
          for (ZCCatalog catalogDAO : catalogSet)
          {
            String propers = catalogDAO.getConfigProps();
            if (StringUtil.isNotNull(propers))
            {
              String isUsing = CronPublishEnable.getValue(propers) ? "Y" : "N";
              if ("Y".equals(isUsing))
              {
                Date startTime = CronPublishStartTime.getValue(propers);
                
                int period = CronPublishPeriod.getValue(propers);
                
                String periodType = CronPublishPeriodType.getValue(propers);
                Date current = new Date();
                if (startTime.getTime() < System.currentTimeMillis())
                {
                  String cron = ScheduleUI.getCronExpression(periodType, period, startTime);
                  current.setSeconds(startTime.getSeconds());
                  if (CronMonitor.isOnTime(current, cron))
                  {
                    LogUtil.info("定时全量发布栏目列表页：" + catalogDAO.getName());
                    //ZQ 2015/05/08 

                    ICatalogType catalogType = (ICatalogType)CatalogTypeService.getInstance().get(catalogDAO.getType());
                    catalogType.publish(catalogDAO, false);
                    
                    //发布栏目上的区块
                    Set<ZCBlock> blocks = new ZCBlock().fetch(new Q("where CatalogID = ? and Status = ?",catalogDAO.getID(), "Published"));
                    List<IPublishPlatform> pts = PublishPlatformService.getUsedPublishPlatform(catalogDAO.getSiteID());
            		for (ZCBlock block : blocks) {
            			for (IPublishPlatform pt : pts) {
            				BlockBL.publish(block, "Block", pt.getExtendItemID(), false);
            			}
            		}
            		
                    publishSite = false;
                    if (CronPublishDetailEnable.getValue(propers)) {
                    	PublishBL.publishCatalogContents(catalogDAO, ContentStatus.PUBLISHED, null);
                    }
                  }
                }
              }
            }
          }
        }
        if (publishSite) {
          PublishBL.publishSiteIndex(dao);
        }
      }
    }
  }
  
  public String getDefaultCronExpression()
  {
    return "* * * * *";
  }
}
