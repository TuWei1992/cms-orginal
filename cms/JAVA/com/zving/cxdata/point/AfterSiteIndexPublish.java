package com.zving.cxdata.point;

import com.zving.contentcore.IPublishPlatform;
import com.zving.contentcore.item.PCPublishPlatform;
import com.zving.contentcore.service.PublishPlatformService;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.IExtendAction;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.schema.ZCSite;

public abstract class AfterSiteIndexPublish implements IExtendAction {
	public static final String ExtendPointID = "com.zving.cxdata.point.AfterSiteIndexPublish";
	@Override
	public Object execute(Object[] args) throws ExtendException {
		// TODO Auto-generated method stub
		ZCSite site = (ZCSite)args[0];
		if (site != null) {
			String publishPlatformID = (String)args[1];
			IPublishPlatform pl = PublishPlatformService.getInstance().get(publishPlatformID);
			if (pl == null) {
				pl = PublishPlatformService.getInstance().get(PCPublishPlatform.ID);
			}
			LongTimeTask tis = null;
			if (args.length > 2) {
				tis = (LongTimeTask)args[2];
			}
			execute(site, pl, tis);
		}
		return null;
	}

	@Override
	public boolean isUsable() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public abstract void execute(ZCSite site, IPublishPlatform platform, LongTimeTask task);

}
