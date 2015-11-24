package com.zving.platform;

import com.zving.framework.data.DataCollection;
import com.zving.framework.extend.IExtendItem;
import com.zving.framework.extend.action.ZhtmlContext;
import com.zving.framework.json.JSONObject;

/**
 * 用户可选项类别
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-30
 */
public interface IPreferencesType extends IExtendItem {

	/**
	 * 业务执行生通过此方法包含文件
	 */
	public void onPageExecute(ZhtmlContext context);

	/**
	 * 可选项保存时通过此方法生成该可选项类别对应的JSONObject对象
	 */
	public JSONObject onSave(DataCollection Request);

}
