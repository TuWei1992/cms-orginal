package com.zving.framework.ui.control.datalist;

import com.zving.framework.collection.Mapx;
import com.zving.framework.ui.control.DataListAction;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;

/**
 * DataList标签体管理器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-4
 */
public class DataListBodyManager {
	static Mapx<String, DataListBody> map = new Mapx<String, DataListBody>();

	public static DataListBody get(DataListAction dla, String uid, String html) {
		if (!map.containsKey(uid)) {
			DataListBody body = new DataListBody(uid, html);
			body.compile(dla);
			map.put(uid, body);
		}
		return map.get(uid);
	}

	public static DataListBody get(String uid) {
		DataListBody body = map.get(uid);
		if (body == null) {// debug模式下使用loadData()会导致此处得到null值
			String fileName = uid.substring(0, uid.indexOf('#'));
			try {
				ZhtmlManagerContext.getInstance().getTemplateManager().getExecutor(fileName);
			} catch (Exception e) {
			}
			body = map.get(uid);
		}
		return body;
	}
}
