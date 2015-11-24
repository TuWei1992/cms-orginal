package com.zving.platform.service;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.platform.IRecyclableItem;

public class RecycleBinService extends AbstractExtendService<IRecyclableItem> {
	public static RecycleBinService getInstance() {
		return findInstance(RecycleBinService.class);
	}
}
