package com.zving.cxdata.ui;

import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.item.PCPublishPlatform;
import com.zving.contentcore.service.PublishPlatformService;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Filter;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.ui.tag.ListAction;

@Alias("platformContentTemplate")
public class PlatformContentTemplateUI extends UIFacade {
	@Priv
	public void getUsedPublishPlatformsExPCList(ListAction dla) {
		dla.bindData(getUsedPublishPlatformsExPC());
	}

	@Priv
	public DataTable getUsedPublishPlatformsExPC() {
		return PublishPlatformService.getUsedPublishPlatformDataTable(SiteBL.getCurrentSite()).filter(new Filter<DataRow>() {
			public boolean filter(DataRow obj) {
				return !obj.get("ID").equals(PCPublishPlatform.ID);
			}
		});
	}

}
