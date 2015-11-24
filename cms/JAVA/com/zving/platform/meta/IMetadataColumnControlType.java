package com.zving.platform.meta;

import com.zving.framework.extend.IExtendItem;
import com.zving.schema.ZDMetaColumn;

/**
 * author: 欧阳晓亮
 * Email: oyxl@zving.com
 * Date: 2013-3-12
 */
public interface IMetadataColumnControlType extends IExtendItem {
	/**
	 * 获得空间类型html代码
	 */
	public String getHtml(ZDMetaColumn mc, String value);

	/**
	 * 获得保存数据类型
	 */
	public String getSaveDataType();

}
