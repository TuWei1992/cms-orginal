package com.zving.platform.api.format;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataCollection;
import com.zving.platform.IAPIDataFormat;
import com.zving.platform.api.APIResponse;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-5-12
 */
public class XMLFormat implements IAPIDataFormat {
	public static final String ID = "XML";

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return null;
	}

	@Override
	public Mapx<String, Object> parse(String params) {
		DataCollection dc = new DataCollection();
		dc.parseXML(params);
		return dc;
	}

	@Override
	public String toString(APIResponse response) {
		DataCollection dc = new DataCollection();
		dc.putAll(response);
		return dc.toXML();
	}
}
