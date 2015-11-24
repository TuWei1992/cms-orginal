package com.zving.platform;

import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.IExtendItem;

public abstract class AbstractUserPreferences implements IExtendItem {
	/**
	 * 属性值是否正确
	 */
	public abstract boolean validate(String value);

	/**
	 * 处理传入的MAP中的值，转换成要保存的值
	 */
	public String process(Mapx<String, Object> map) {
		return map.getString(getExtendItemID());
	}

	/**
	 * 默认值
	 */
	public abstract String defaultValue();
}
