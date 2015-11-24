package com.zving.platform.log.type;

import java.util.Map;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.extend.menu.Menu;
import com.zving.framework.extend.menu.MenuManager;
import com.zving.framework.i18n.Lang;
import com.zving.platform.ILogType;

/**
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-3-30
 */
public class UserLog implements ILogType {
	public static final String ID = "UserLog";

	public static final String SubType_Login = "Login";

	private Mapx<String, String> map;

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Logs.UserLogMenu}";
	}

	@Override
	public Mapx<String, String> getSubTypes() {
		if (map == null) {
			map = new Mapx<String, String>();
			Map<String, Menu> menus = MenuManager.getMenus();
			if (menus != null && menus.size() > 0) {
				for (Menu menu : menus.values()) {
					map.put("Visit " + menu.getID(), menu.getName());
				}
			}
			map.put("User Login", Lang.get("Platform.UserLogin"));
		}
		return map;
	}

	@Override
	public void decodeMessage(DataTable dt) {
		dt.insertColumn("LogMessageName");
		Mapx<String, String> map = getSubTypes();
		for (DataRow dr : dt) {
			String message = dr.getString("LogMessage");
			dr.set("LogMessageName", map.get(message));
		}
	}

}
