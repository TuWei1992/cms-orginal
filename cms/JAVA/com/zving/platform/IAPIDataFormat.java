package com.zving.platform;

import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.IExtendItem;
import com.zving.platform.api.APIResponse;

/**
 * API数据格式
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-5-12
 */
public interface IAPIDataFormat extends IExtendItem {
	public Mapx<String, Object> parse(String params);

	public String toString(APIResponse response);
}
