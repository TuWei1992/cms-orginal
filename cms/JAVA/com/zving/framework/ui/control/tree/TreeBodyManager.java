package com.zving.framework.ui.control.tree;

import com.zving.framework.collection.Mapx;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.ui.zhtml.ZhtmlManagerContext;

/**
 * 树标签体管理器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-4
 */
public class TreeBodyManager {
	private static Mapx<String, TreeBody> map = new Mapx<String, TreeBody>();

	public static TreeBody get(TreeAction dla, String uid, String html) {
		if (!map.containsKey(uid)) {
			TreeBody body = new TreeBody(uid, html);
			body.compile(dla);
			map.put(uid, body);
		}
		return map.get(uid);
	}

	public static TreeBody get(String uid) {
		TreeBody body = map.get(uid);
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
