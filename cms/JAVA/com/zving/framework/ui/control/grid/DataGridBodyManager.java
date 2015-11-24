package com.zving.framework.ui.control.grid;

import com.zving.framework.collection.Mapx;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;

/**
 * DataGrid标签体管理器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-4
 */
public class DataGridBodyManager {
	static Mapx<String, DataGridBody> map = new Mapx<String, DataGridBody>();

	public static DataGridBody get(DataGridAction dga, String uid, String html) {
		if (!map.containsKey(uid)) {
			DataGridBody body = new DataGridBody(uid, html);
			body.compile(dga);
			map.put(uid, body);
		}
		return map.get(uid);
	}

	public static DataGridBody get(String uid) {
		DataGridBody body = map.get(uid);
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
