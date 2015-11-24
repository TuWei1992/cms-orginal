package com.zving.framework.json.convert;

import com.zving.framework.extend.AbstractExtendService;

/**
 * JSON转换器扩展服务类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-17
 */
public class JSONConvertorService extends AbstractExtendService<IJSONConvertor> {
	public static JSONConvertorService getInstance() {
		return AbstractExtendService.findInstance(JSONConvertorService.class);
	}

}
