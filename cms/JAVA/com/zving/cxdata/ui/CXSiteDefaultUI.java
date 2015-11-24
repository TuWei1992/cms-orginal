package com.zving.cxdata.ui;

import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.config.CMSContextURL;
import com.zving.contentcore.property.impl.FrontAppContextAddr;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.cxdata.ICXDataCondition;
import com.zving.cxdata.property.CXDataPublishConditionProp;
import com.zving.cxdata.service.CXDataConditionService;
import com.zving.framework.Config;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.Lang;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.util.MessageCache;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCSite;
import com.zving.schema.ZDUser;
import com.zving.stat.report.SummaryReportUI;

@Alias("CXSiteDefault")
public class CXSiteDefaultUI extends UIFacade {
	
	@Priv
	 public void initDefault() {
		 long id = SiteBL.getCurrentSite();
		    if (id == 0L) {
		      fail("无任何站点权限");
		      return;
		    }
		    $S("ID", Long.valueOf(id));
		    $S("SiteURL", SiteUtil.getDAO(id).getURL());
		    ZDUser user = new ZDUser();
		    user.setUserName(User.getUserName());
		    user.fill();
		    if (ObjectUtil.notEmpty(user.getLastLoginTime())) {
		      $S("LastLoginTime", user.getLastLoginTime());
		    } else {
		      $S("LastLoginTime", Lang.get("Contentcore.UnKnow"));
		    }
		    if (ObjectUtil.notEmpty(user.getLastLoginIP())) {
		      $S("LastLoginIP", user.getLastLoginIP());
		    } else {
		      $S("LastLoginIP", "");
		    }
		    long catalogID = $L("CatalogID");
		    if (catalogID != 0L)
		    {
		      ZCCatalog catalog = CatalogUtil.getDAO(catalogID);
		      if (catalog != null) {
		        $S("CatalogName", catalog.getName());
		      }
		    }
		    $S("MessageCount", Integer.valueOf(MessageCache.getNoReadCount()));
		    $S("CMSContextURL", CMSContextURL.getValue());
		    $S("FrontAppContextAddr", FrontAppContextAddr.getValue(id, false));
		    $S("ProductName", Config.getAppName());
		    $S("CurrentUser", StringUtil.isEmpty(User.getRealName()) ? User.getUserName() : User.getRealName());

		  
		    ZCSite site = SiteUtil.getDAO(id);
		    String conProp =CXDataPublishConditionProp.getValue(site.getConfigProps());
			String searchCondition = "";
			String searchConditionParam = "";
			if (StringUtil.isNotEmpty(conProp)) {
			ICXDataCondition condition = CXDataConditionService.getInstance().get(conProp);
				if (condition != null) {
					  searchCondition +=  condition.getExtendItemName() +":";
					  searchCondition += condition.getFormHtml();
					  searchCondition += "  ";
					  searchConditionParam += condition.getArgName()+",";
				}
			}
			$S("searchCondition", searchCondition);
			$S("searchConditionParam", searchConditionParam);
		    
	 }
}
