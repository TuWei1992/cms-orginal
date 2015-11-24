package com.zving.cxdata.action;

import java.util.HashMap;
import java.util.Map;

import com.zving.contentcore.IPublishPlatform;
import com.zving.cxdata.ICXDataCondition;
import com.zving.cxdata.bl.CXPublishBL;
import com.zving.cxdata.point.AfterSiteIndexPublish;
import com.zving.cxdata.property.CXDataPublishConditionPlatform;
import com.zving.cxdata.property.CXDataPublishConditionProp;
import com.zving.cxdata.service.CXDataConditionService;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCSite;

public class AfterSiteIndexPublishAction extends AfterSiteIndexPublish {

	@Override
	public void execute(ZCSite site, IPublishPlatform platform, LongTimeTask task) {
		try {
			String publishCondition = CXDataPublishConditionProp.getValue(site.getConfigProps());
			String publishConditionPlatform = CXDataPublishConditionPlatform.getValue(site.getConfigProps());
			
			if (StringUtil.isNotEmpty(publishCondition) && StringUtil.isNotEmpty(publishConditionPlatform) 
					&& publishConditionPlatform.indexOf(platform.getExtendItemID()) != -1) {
				ICXDataCondition con = CXDataConditionService.getInstance().get(publishCondition);
				if (con != null) {
					DataTable conditions = con.getOptions();
					int total = conditions.getRowCount();
					for (int i = 0; i < total; i++) {
						LogUtil.info("正在发布首页：" + conditions.get(i, 1));
						if (task != null) {
							 task.setCurrentInfo("正在发布首页：" + conditions.get(i, 1));
						     task.setPercent((i + 1) * 100 / total);
						}
						Map cxParam = new HashMap();
						cxParam.put(con.getArgName(), conditions.get(i, 0));
						cxParam.put(con.getArgName()+"_Text", conditions.get(i, 1));
						CXPublishBL.publishSite(site, platform.getExtendItemID(), conditions.getString(i, 0), cxParam);
						if (Errorx.hasError()) {
							if (task != null) {
								//task.setFinishedInfo(Errorx.getAllMessage());
								task.addError(Errorx.getAllMessage());
							}
							return;
						}
					}
				}
			}
		} catch (Exception e) {
			if (task != null) {
				task.addError(e.getMessage());
			}
			Errorx.addError(e.getMessage());
		}
	}
}
