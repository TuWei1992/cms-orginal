package com.zving.platform.ui;

import java.util.LinkedList;
import java.util.Map;

import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Filter;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.menu.Menu;
import com.zving.framework.extend.menu.MenuManager;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.security.Privilege;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.privilege.MenuPriv;
import com.zving.platform.service.MenuPrivService;

/**
 * @Author 王育春
 * @Date 2007-6-19
 * @Mail wyuch@zving.com
 */
@Alias("Menu")
public class MenuUI extends UIFacade {

	@Priv(MenuPriv.MenuID)
	public void del() {
		String[] arr = $V("IDs").split(",");
		for (String id : arr) {
			Menu m = MenuManager.getMenu(id);
			PluginConfig pc = m.getPluginConfig();
			if (pc == null) {
				MenuManager.deleteMenu(m);
			} else {
				Errorx.addError(Lang.get("Platform.Menu.MenudeleteFailed", new Object[] { m.getID() }));
			}
		}
		if (Errorx.hasError()) {
			success(Lang.get("Menu.Success") + "," + Lang.get("Platform.Menu.partError") + Errorx.getAllMessage());
		} else {
			success(Lang.get("Menu.Success"));
		}
	}

	@Priv(MenuPriv.MenuID)
	public void bindSelectorTree(TreeAction ta) {
		ta.setRootText("Root");
		DataTable dt = MenuPrivService.getAllMenus();
		dt = dt.filter(new Filter<DataRow>() {
			@Override
			public boolean filter(DataRow dr) {
				if (dr.getString("ParentID").equals("")) {
					return true;
				}
				return false;
			}
		});
		ta.bindData(dt);
	}

	@Priv
	public void bindMenuList(DataGridAction dga) {
		DataTable dt = MenuPrivService.getAllMenus(false);
		dt = dt.filter(new Filter<DataRow>() {
			@Override
			public boolean filter(DataRow dr) {
				return PrivCheck.check(dr.getString("ID"));
			}
		});
		dt.insertColumn("Status");
		for (DataRow dr : dt) {
			dr.set("Status", ExtendManager.getInstance().isMenuEnable(dr.getString("ID")) ? "Y" : "N");
		}
		YesOrNo.decodeYesOrNoIcon(dt, "Status", false);
		dga.bindData(dt);
	}

	@Priv(MenuPriv.MenuID)
	public void dg1DataBind(DataGridAction dga) {
		final String pluginID = $V("PluginID");
		DataTable dt = MenuPrivService.getAllMenus();
		if (ObjectUtil.notEmpty(pluginID)) {
			dt = dt.filter(new Filter<DataRow>() {
				@Override
				public boolean filter(DataRow dr) {
					if (dr.getString("PluginID").equals(pluginID)) {
						return true;
					}
					return false;
				}
			});
		}
		dt.insertColumn("Status");
		for (DataRow dr : dt) {
			dr.set("Status", ExtendManager.getInstance().isMenuEnable(dr.getString("ID")) ? "Y" : "N");
			String icon = dr.getString("Icon");
			icon = icon.substring(icon.indexOf('/') + 1, icon.indexOf('.'));
			dr.set("Icon", icon);
		}
		YesOrNo.decodeYesOrNoIcon(dt, "Status", false);
		dga.bindData(dt);
	}

	@Priv
	public DataTable getFirstLevelMenu() {
		DataTable dt = MenuPrivService.getMainMenus();
		dt.deleteColumn("ParentID");
		dt.deleteColumn("Icon");
		dt.deleteColumn("URL");
		dt.deleteColumn("Order");
		dt.deleteColumn("Description");
		dt.deleteColumn("PluginID");
		dt.deleteColumn("PluginName");
		dt.deleteColumn("HasChild");
		return dt;
	}

	@Priv(MenuPriv.MenuID)
	public void init() {
		if (StringUtil.isEmpty($V("ID"))) {
			return;
		}
		Menu m = MenuManager.getMenu($V("ID"));
		$S("ID", m.getID());
		$S("Name", m.getName());
		$S("Description", m.getDescription());
		$S("URL", m.getURL() == null ? "" : m.getURL());
		$S("Icon", m.getIcon());
		$S("ParentID", m.getParentID());
		Menu parent = null;
		if (ObjectUtil.notEmpty(m.getParentID())) {
			parent = MenuManager.getMenu(m.getParentID());
		}

		if (parent != null) {
			$S("ParentName", MenuManager.getMenu(m.getParentID()).getName());
		}
		if (ObjectUtil.notEmpty(m.getPluginConfig())) {
			$S("PluginName", m.getPluginConfig().getName());
		}
	}

	@Priv(MenuPriv.MenuID)
	public void add() {
		String id = $V("ID");
		if (StringUtil.isEmpty(id)) {
			fail("ID不能为空");
			return;
		}
		Menu m = new Menu();
		m.setID(id);
		m.setIcon("icons/icon001a1.png");
		m.setType(Menu.Type_Backend);
		m.setName(LangUtil.getI18nFieldValue("Name"));
		m.setParentID($V("ParentID"));
		m.setURL($V("URL"));
		m.setDescription($V("Description"));

		MenuManager.addMenu(m);

		if (Errorx.hasError()) {
			fail(Lang.get("Common.ExecuteFailed") + ":" + Errorx.getAllMessage());
		} else {
			success(Lang.get("Common.ExecuteSuccess"));
		}
	}

	@Priv(MenuPriv.MenuID)
	public void save() {
		if (StringUtil.isEmpty($V("ID"))) {
			return;
		}
		Menu m = MenuManager.getMenu($V("ID"));
		m.setName(LangUtil.getI18nFieldValue("Name"));
		m.setParentID($V("ParentID"));
		// m.setURL($V("URL"));
		// m.setDescription($V("Description"));
		MenuManager.editMenu(m);
		if (Errorx.hasError()) {
			fail(Lang.get("Common.ExecuteFailed") + ":" + Errorx.getAllMessage());
		} else {
			success(Lang.get("Common.ExecuteSuccess"));
		}
	}

	@Priv(MenuPriv.MenuID)
	public DataTable getPluginList() {
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Name");
		for (PluginConfig pc : PluginManager.getInstance().getAllPluginConfig()) {
			if (pc.getMenus() != null && pc.getMenus().size() > 0) {
				dt.insertRow(new Object[] { pc.getID(), pc.getName() });
			}
		}
		return dt;
	}

	@Priv(MenuPriv.Start + Privilege.Or + MenuPriv.Stop)
	public void setStatus() {
		String status = $V("Status");
		String[] ids = $V("IDs").split(",");
		for (String id : ids) {
			if (ObjectUtil.empty(id)) {
				continue;
			}
			Menu m = MenuManager.getMenu(id);
			String failed = Lang.get("Common.ExecuteFailed");

			if (m == null) {
				fail(failed + ":Menu not found");
				return;
			}
			if ("true".equals(status)) {
				if (!ExtendManager.getInstance().isMenuEnable(id)) {
					ExtendManager.getInstance().enableMenu(id);
				}
			} else {
				if (ExtendManager.getInstance().isMenuEnable(id)) {
					// 警用父级目录，子菜单也要被禁用
					Map<String, Menu> menus = MenuManager.getMenus();
					for (Menu mm : menus.values()) {
						String pid = mm.getParentID();
						if (ObjectUtil.notEmpty(pid) && pid.equals(id)) {
							ExtendManager.getInstance().disableMenu(mm.getID());
						}
					}
					ExtendManager.getInstance().disableMenu(id);
				}
			}
		}
		success(Lang.get("Common.ExecuteSuccess"));
	}

	@Priv(MenuPriv.Sort)
	public void sortMenu() {
		int move = Request.getInt("Move"); // 移动量，正数上移，负数下移
		if (StringUtil.isEmpty($V("ID"))) {
			return;
		}
		Menu m = MenuManager.getMenu($V("ID"));
		final String parentID = m.getParentID() == null ? "" : m.getParentID();
		DataTable dt = MenuPrivService.getAllMenus();
		DataTable dt2 = dt.filter(new Filter<DataRow>() {
			// 过滤掉非同级目录
			@Override
			public boolean filter(DataRow obj) {
				return ObjectUtil.equal(parentID, obj.getString("ParentID"));
			}
		});
		int index = -1;
		LinkedList<DataRow> list = new LinkedList<DataRow>();
		DataRow temp = null;
		for (int i = 0; i < dt2.getRowCount(); i++) {
			if (dt2.get(i, "ID").equals(m.getID())) {
				index = i;
				temp = dt2.get(i);
				continue;
			}
			list.add(dt2.get(i));
		}
		index -= move;
		if (index > 0 && index > list.size()) {
			index = list.size();
		}
		if (index < 0) {
			index = 0;
		}
		list.add(index, temp);
		int i = 10;
		for (DataRow dr : list) {
			Menu menu = MenuManager.getMenu(dr.getString("ID"));
			menu.setOrder(Integer.toString(i));
			MenuManager.editMenu(menu);
			i++;
		}

		if (Current.getTransaction().commit()) {
			success(Lang.get("Code.SortSuccess"));
		} else {
			fail(Lang.get("Code.SortFailed"));
		}
	}

	@Priv(MenuPriv.Reset)
	public void reset() {
		String fileName = Config.getContextRealPath() + "WEB-INF/plugins/classes/plugins/menu.config";
		FileUtil.delete(fileName);
		MenuManager.reloadMenus();
		success(Lang.get("Common.ExecuteSuccess"));
	}

}
