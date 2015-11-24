package com.zving.platform.meta;

import com.zving.framework.extend.AbstractExtendService;

/**
 * 元数据服务，便于各插件注册元数据类型。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2012-1-13
 */
public class MetadataService extends AbstractExtendService<IMetaModelType> {

	public static final String ControlPrefix = "MetaValue_";

	public static MetadataService getInstance() {
		return findInstance(MetadataService.class);
	}

}
