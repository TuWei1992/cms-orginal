package com.zving.platform;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.extend.IExtendItem;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-14
 */
public interface ILogType extends IExtendItem {
	/**
	 * 根据字类型ID返回字类型名称
	 */
	public Mapx<String, String> getSubTypes();

	/**
	 * 将Message解析为可读的信息
	 */
	public void decodeMessage(DataTable dt);
}
