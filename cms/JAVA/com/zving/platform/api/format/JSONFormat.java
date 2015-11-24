package com.zving.platform.api.format;

import com.zving.framework.collection.Mapx;
import com.zving.framework.json.JSON;
import com.zving.platform.IAPIDataFormat;
import com.zving.platform.api.APIResponse;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-5-12
 */
public class JSONFormat implements IAPIDataFormat {
	public static final String ID = "JSON";

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
		return JSON.parseJSONObject(params);
	}

	@Override
	public String toString(APIResponse response) {
		return JSON.toJSONString(response);
	}

}
