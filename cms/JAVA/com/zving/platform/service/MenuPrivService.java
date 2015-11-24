package com.zving.platform.service;

import java.util.Comparator;
import java.util.Map;

import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.extend.AbstractExtendService;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.menu.Menu;
import com.zving.framework.extend.menu.MenuManager;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.config.MenuOrder;
import com.zving.platform.privilege.AbstractMenuPriv;

/*
 * @Author 王育春
 * @Date 2011-1-18
 * @Mail wyuch@zving.com
 */
public class MenuPrivService extends AbstractExtendService<AbstractMenuPriv> {
	public static MenuPrivService getInstance() {
		return findInstance(MenuPrivService.class);
	}

	public static DataTable getAllMenus(boolean loadAll) {
		Map<String, Menu> list = MenuManager.getMenus();
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("ParentID");
		dt.insertColumn("Name");
		dt.insertColumn("Icon");
		dt.insertColumn("URL");
		dt.insertColumn("Order");
		dt.insertColumn("Description");
		dt.insertColumn("PluginID");
		dt.insertColumn("PluginName");
		dt.insertColumn("HasChild");
		for (Menu menu : list.values()) {
			if (!Menu.Type_Backend.equals(menu.getType())) {
				continue;
			}
			if (!loadAll && !ExtendManager.getInstance().isMenuEnable(menu.getID())) {
				continue;
			}
			if (ObjectUtil.notEmpty(menu.getPluginConfig())) {
				dt.insertRow(new Object[] { menu.getID(), menu.getParentID(), menu.getName(), menu.getIcon(), menu.getURL(),
						menu.getOrder(), menu.getDescription(), menu.getPluginConfig().getID(), menu.getPluginConfig().getName(),
						hasChild(menu.getID()) });
			} else {
				dt.insertRow(new Object[] { menu.getID(), menu.getParentID(), menu.getName(), menu.getIcon(), menu.getURL(),
						menu.getOrder(), menu.getDescription(), "", "", hasChild(menu.getID()) });
			}
		}
		sortMenu(dt);
		dt = DataGridAction.sortTreeDataTable(dt, "ID", "ParentID");
		return dt;

	}

	public static DataTable getAllMenus() {
		return getAllMenus(true);
	}

	public static boolean hasChild(String menuID) {
		Map<String, Menu> list = MenuManager.getMenus();
		for (Menu menu : list.values()) {
			if (menu.getParentID() != null && menu.getParentID().equals(menuID)) {
				return true;
			}
		}
		return false;
	}

	public static DataTable getChildMenus(String parentID) {
		Map<String, Menu> list = MenuManager.getMenus();
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("ParentID");
		dt.insertColumn("Name");
		dt.insertColumn("Icon");
		dt.insertColumn("URL");
		dt.insertColumn("Order");
		dt.insertColumn("Description");
		dt.insertColumn("PluginID");
		dt.insertColumn("PluginName");
		dt.insertColumn("HasChild");
		for (Menu menu : list.values()) {
			if (menu.getParentID() == null || !menu.getParentID().equals(parentID)) {
				continue;
			}
			if (!ExtendManager.getInstance().isMenuEnable(menu.getID())) {
				continue;
			}
			if (ObjectUtil.notEmpty(menu.getPluginConfig())) {
				dt.insertRow(new Object[] { menu.getID(), menu.getParentID(), menu.getName(), menu.getIcon(), menu.getURL(),
						menu.getOrder(), menu.getDescription(), menu.getPluginConfig().getID(), menu.getPluginConfig().getName(),
						hasChild(menu.getID()) });
			} else {
				dt.insertRow(new Object[] { menu.getID(), menu.getParentID(), menu.getName(), menu.getIcon(), menu.getURL(),
						menu.getOrder(), menu.getDescription(), "", "", hasChild(menu.getID()) });

			}
		}
		dt.sort("Order", "asc");
		return dt;
	}

	public static DataTable getMainMenus() {
		Map<String, Menu> list = MenuManager.getMenus();
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("ParentID");
		dt.insertColumn("Name");
		dt.insertColumn("Icon");
		dt.insertColumn("URL");
		dt.insertColumn("Order");
		dt.insertColumn("Description");
		dt.insertColumn("PluginID");
		dt.insertColumn("PluginName");
		dt.insertColumn("HasChild");
		for (Menu menu : list.values()) {
			if (!ObjectUtil.empty(menu.getParentID())) {
				continue;
			}
			if (!ExtendManager.getInstance().isMenuEnable(menu.getID())) {
				continue;
			}
			if (ObjectUtil.notEmpty(menu.getPluginConfig())) {
				dt.insertRow(new Object[] { menu.getID(), menu.getParentID(), menu.getName(), menu.getIcon(), menu.getURL(),
						menu.getOrder(), menu.getDescription(), menu.getPluginConfig().getID(), menu.getPluginConfig().getName(),
						hasChild(menu.getID()) });
			} else {
				dt.insertRow(new Object[] { menu.getID(), menu.getParentID(), menu.getName(), menu.getIcon(), menu.getURL(),
						menu.getOrder(), menu.getDescription(), "", "", hasChild(menu.getID()) });

			}
		}
		sortMenu(dt);
		return dt;
	}

	private static void sortMenu(DataTable dt) {
		dt.sort("Order", "asc");
		String str = MenuOrder.getValue() + "\n";
		final String order = str.replaceAll("\\s+", "\n");
		dt.sort(new Comparator<DataRow>() {
			@Override
			public int compare(DataRow dr1, DataRow dr2) {
				String str1 = dr1.getString("ID") + "\n";
				String str2 = dr2.getString("ID") + "\n";
				int i1 = order.indexOf(str1);
				int i2 = order.indexOf(str2);
				if (i1 >= 0 && i2 < 0) {
					return -1;
				}
				if (i2 >= 0 && i1 < 0) {
					return 1;
				}
				return i1 - i2;
			}
		});

	}
}
