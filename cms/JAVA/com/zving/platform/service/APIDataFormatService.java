package com.zving.platform.service;

import com.zving.framework.extend.AbstractExtendService;
import com.zving.platform.IAPIDataFormat;

public class APIDataFormatService extends AbstractExtendService<IAPIDataFormat> {

	public static APIDataFormatService getInstance() {
		APIDataFormatService dts = findInstance(APIDataFormatService.class);
		return dts;
	}

}
