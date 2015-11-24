package com.zving.platform.ui;

import java.util.List;
import java.util.Map;

import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.extend.ExtendActionConfig;
import com.zving.framework.extend.ExtendItemConfig;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.extend.ExtendPointConfig;
import com.zving.framework.extend.ExtendServiceConfig;
import com.zving.framework.extend.menu.Menu;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginException;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.util.PlatformUtil;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-8
 */
@Alias("Plugin")
public class PluginUI extends UIFacade {

	@Priv
	public void bindTree(TreeAction ta) {
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Name");
		dt.insertColumn("Author");
		dt.insertColumn("Version");
		for (PluginConfig pc : PluginManager.getInstance().getAllPluginConfig()) {
			dt.insertRow(new Object[] { pc.getID(), pc.getName(User.getLanguage()), pc.getAuthor(), pc.getVersion() });
		}
		ta.setRootText(Lang.get("Platform.PluginList"));
		dt.insertColumn("Icon", "icons/icon024a1.png");
		for (int i = 1; i < dt.getRowCount(); i++) {
			if (!PluginManager.getInstance().getPluginConfig(dt.getString(i, "ID")).isEnabled()) {
				dt.set(i, "Icon", "icons/icon024a17.png");
			}
		}
		ta.bindData(dt);
	}

	@Priv
	public void initBasicInfo() {
		String id = $V("ID");
		PluginConfig pc = PluginManager.getInstance().getPluginConfig(id);
		if (pc != null) {
			$S("ID", pc.getID());
			$S("Name", pc.getName(User.getLanguage()));
			$S("Version", pc.getVersion());
			$S("Author", pc.getAuthor());
			$S("Provider", pc.getProvider());
			if (pc.isEnabled()) {
				$S("Status", Lang.get("User.Enable"));
			} else {
				$S("Status", Lang.get("User.Disable"));
			}
			$S("PluginClass", pc.getClassName());
			$S("Description", StringUtil.htmlEncode(pc.getDescription(User.getLanguage())));
		}
	}

	@Priv
	public void bindPluginGrid(DataGridAction dga) {
		PluginConfig pc = PluginManager.getInstance().getPluginConfig($V("ID"));
		if (pc != null) {
			DataTable dt = new DataTable();
			dt.insertColumn("ID");
			dt.insertColumn("Name");
			dt.insertColumn("Version");
			for (String id : pc.getRequiredPlugins().keySet()) {
				PluginConfig pc2 = PluginManager.getInstance().getPluginConfig(id);
				dt.insertRow(new Object[] { pc2.getID(), pc2.getName(User.getLanguage()), pc2.getVersion() });
			}
			dga.bindData(dt);
		}
	}

	@Priv
	public void bindDependOnPluginGrid(DataGridAction dga) {
		PluginConfig pc = PluginManager.getInstance().getPluginConfig($V("ID"));
		Mapx<String, PluginConfig> map = new Mapx<String, PluginConfig>();
		if (pc != null) {
			List<PluginConfig> list = PluginManager.getInstance().getAllPluginConfig();
			addDependOn(map, list, pc.getID());
		}
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Name");
		dt.insertColumn("Version");
		for (PluginConfig pc2 : map.values()) {
			dt.insertRow(new Object[] { pc2.getID(), pc2.getName(User.getLanguage()), pc2.getVersion() });
		}
		dga.bindData(dt);

	}

	private void addDependOn(Map<String, PluginConfig> map, List<PluginConfig> list, String id) {
		for (PluginConfig pc : list) {
			if (pc.getRequiredPlugins().containsKey(id)) {
				if (!map.containsKey(pc.getID())) {
					map.put(pc.getID(), pc);// 防止无限循环
					addDependOn(map, list, pc.getID());
				}
			}
		}
	}

	@Priv
	public void bindExtendPointGrid(DataGridAction dga) {
		PluginConfig pc = PluginManager.getInstance().getPluginConfig($V("ID"));
		if (pc == null) {
			return;
		}
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Icon");
		dt.insertColumn("ClassName");
		dt.insertColumn("UIFlag");
		dt.insertColumn("Description");
		for (ExtendPointConfig ep : pc.getExtendPoints().values()) {
			dt.insertRow(new Object[] { ep.getID(), "platform/images/plugin_point.gif", ep.getClassName(), ep.getUIFlag(),
					StringUtil.htmlEncode(ep.getDescription(User.getLanguage())) });
		}
		dga.bindData(dt);
	}

	@Priv
	public void bindExtendServiceGrid(DataGridAction dga) {
		PluginConfig pc = PluginManager.getInstance().getPluginConfig($V("ID"));
		if (pc == null) {
			return;
		}
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Icon");
		dt.insertColumn("ClassName");
		dt.insertColumn("Description");
		for (ExtendServiceConfig ep : pc.getExtendServices().values()) {
			dt.insertRow(new Object[] { ep.getID(), "platform/images/plugin_point.gif", ep.getClassName(),
					StringUtil.htmlEncode(ep.getDescription(User.getLanguage())) });
		}
		dga.bindData(dt);
	}

	@Priv
	public void bindExtendItemGrid(DataGridAction dga) {
		PluginConfig pc = PluginManager.getInstance().getPluginConfig($V("ID"));
		if (pc == null) {
			return;
		}
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Icon");
		dt.insertColumn("ClassName");
		dt.insertColumn("Description");
		for (ExtendItemConfig ep : pc.getExtendItems().values()) {
			dt.insertRow(new Object[] { ep.getID(), "platform/images/plugin_point.gif", ep.getClassName(),
					StringUtil.htmlEncode(ep.getDescription(User.getLanguage())) });
		}
		dga.bindData(dt);
	}

	@Priv
	public void bindExtendActionGrid(DataGridAction dga) {
		PluginConfig pc = PluginManager.getInstance().getPluginConfig($V("ID"));
		if (pc == null) {
			return;
		}
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Icon");
		dt.insertColumn("ClassName");
		dt.insertColumn("ExtendPointID");
		dt.insertColumn("Description");

		for (ExtendActionConfig ep : pc.getExtendActions().values()) {
			dt.insertRow(new Object[] { ep.getID(), "platform/images/plugin_action.gif", ep.getClassName(), ep.getExtendPointID(),
					StringUtil.htmlEncode(ep.getDescription(User.getLanguage())) });
		}
		dga.bindData(dt);
	}

	@Priv
	public void bindMenuGrid(DataGridAction dga) {
		String PluginID = $V("ID");
		PluginConfig pc = PluginManager.getInstance().getPluginConfig(PluginID);
		if (pc == null) {
			return;
		}
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
		for (Menu menu : pc.getMenus().values()) {
			if (!Menu.Type_Backend.equals(menu.getType())) {
				continue;
			}
			dt.insertRow(new Object[] { menu.getID(), menu.getParentID(), menu.getName(User.getLanguage()), menu.getIcon(), menu.getURL(),
					menu.getOrder(), StringUtil.htmlEncode(menu.getDescription(User.getLanguage())), menu.getPluginConfig().getID(),
					menu.getPluginConfig().getName() });
		}
		dt.sort("Order", "asc");
		dga.bindData(dt);
	}

	@Priv
	public void bindFileTree(TreeAction ta) {
		PluginConfig pc = PluginManager.getInstance().getPluginConfig($V("ID"));
		DataTable dt = new DataTable();
		if (pc != null) {
			dt.insertColumn("Icon");
			dt.insertColumn("ParentID");
			dt.insertColumn("ID");
			dt.insertColumn("Name");
			Mapx<String, String> map = new Mapx<String, String>();
			for (String path : pc.getPluginFiles()) {
				setParentAndName(dt, map, path);
			}
			ta.setRootText(Lang.get("Plugin.PluginFiles"));
		}
		ta.bindData(dt);
	}

	private void setParentAndName(DataTable dt, Mapx<String, String> map, String path) {
		if (map.containsKey(path)) {
			return;
		}
		dt.insertRow((Object[]) null);
		DataRow dr = dt.getDataRow(dt.getRowCount() - 1);
		if (path.startsWith("[D]")) {
			path = path.substring(3);
			if (!path.endsWith("/")) {
				path += "/";
			}
		}
		dr.set("Icon", PlatformUtil.getFileIcon(path));
		map.put(path, "");
		dr.set("ID", path);

		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path.indexOf("/") > 0) {
			String p = path.substring(0, path.lastIndexOf("/") + 1);
			dr.set("ParentID", p);
			dr.set("Name", path.substring(path.lastIndexOf("/") + 1));
			setParentAndName(dt, map, p);
		} else {
			dr.set("ParentID", "");
			dr.set("Name", path);
		}
	}

	@Priv
	public void setStatus() {
		String id = $V("ID");
		String status = $V("Status");
		PluginConfig pc = PluginManager.getInstance().getPluginConfig(id);
		String failed = Lang.get("Common.ExecuteFailed");
		if (pc == null) {
			fail(failed + ":Plugin not found!");
			return;
		}
		try {
			if ("true".equals(status)) {
				if (pc.isEnabled()) {
					fail(failed + ":" + Lang.get("Platform.PluginAlreadyEnable"));
					return;
				} else {
					ExtendManager.getInstance().enablePlugin(id);
				}
			} else {
				if (!pc.isEnabled()) {
					fail(failed + ":" + Lang.get("Platform.PluginAlreadyDisabled"));
					return;
				} else {
					ExtendManager.getInstance().disablePlugin(id);
				}
			}
		} catch (PluginException e) {
			e.printStackTrace();
			fail(failed + ":" + e.getMessage());
			return;
		}
		success(Lang.get("Common.ExecuteSuccess"));
	}
}
