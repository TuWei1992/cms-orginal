package com.zving.cxdata.ui;

import com.zving.contentcore.ICatalogType;
import com.zving.contentcore.bl.ContentBL;
import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.cxdata.ICXDataCondition;
import com.zving.cxdata.bl.CXPublishBL;
import com.zving.cxdata.property.CXDataPublishConditionProp;
import com.zving.cxdata.service.CXDataConditionService;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.Q;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCSite;

@Alias("CXPublish")
public class CXPublishUI extends UIFacade {
	 @Priv("com.zving.cms.Site.Publish.${SiteID}")
	 @Alias(value="Site.publishIndex", alone=true)
	  public void publishIndex() {
		 final String siteID = $V("SiteID");
		 LongTimeTask ltt = LongTimeTask.getInstanceByType("PublishSite");
		    if ((ltt != null) && (ltt.isAlive())) {
		      fail(Lang.get("Contentcore.SitePublisTaskIsRunning"));
		      return;
		    }
		    ltt = new LongTimeTask() {
		      public void execute(){
		  	    CXPublishBL.publishSiteIndex(SiteUtil.getDAO(siteID), this);
		      }
		    };
		    ltt.setType("PublishSite");
		    ltt.setUser(User.getCurrent());
		    ltt.start();
		    $S("TaskID", Long.valueOf(ltt.getTaskID()));
	  }
	  
	 @Priv("com.zving.cms.Site.Publish.${SiteID}")
	  public void publishConditionIndex() {
		 ZCSite site = SiteUtil.getDAO($V("SiteID"));
		 String cb = null;
		 String publishCondition = CXDataPublishConditionProp.getValue(site.getConfigProps());
			if (StringUtil.isNotEmpty(publishCondition)) {
				ICXDataCondition con = CXDataConditionService.getInstance().get(publishCondition);
				if (con != null) {
					String conditionValue = $V(con.getArgName());
					if (StringUtil.isNotEmpty(conditionValue)) {
						cb = conditionValue;
					}
				}
			}
	    if (StringUtil.isNotEmpty(cb)) {
	    	CXPublishBL.publishConditionSiteIndex(site, cb, this.Request);
	    }
	    
	    if (Errorx.hasError()) {
	      fail(Lang.get("Contentcore.PublishFailureMessage1") + ":" + Errorx.getAllMessage());
	    } else {
	      success("发布成功！");
	    }
	  }

	 
}
