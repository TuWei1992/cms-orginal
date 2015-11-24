package com.zving.framework.json.convert;

import com.zving.framework.extend.IExtendItem;
import com.zving.framework.json.JSONObject;

/**
 * JSON转换器接口
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-17
 */
public interface IJSONConvertor extends IExtendItem {
	/**
	 * 是否处理当前对象
	 */
	public boolean match(Object obj);

	/**
	 * 将指定对象输出成JSON
	 */
	public JSONObject toJSON(Object obj);

	/**
	 * 将JSON解析后的Map转化成相应类型的对象
	 */
	public Object fromJSON(JSONObject map);
}
