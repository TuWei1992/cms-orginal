package com.zving.platform.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTable;
import com.zving.framework.extend.ExtendItemConfig;
import com.zving.framework.extend.plugin.PluginConfig;
import com.zving.framework.extend.plugin.PluginManager;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.security.Privilege;
import com.zving.framework.ui.tag.ListAction;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.IAPIMethod;
import com.zving.platform.api.APIUtil;
import com.zving.platform.bl.PrivBL;
import com.zving.platform.privilege.BranchPriv;
import com.zving.platform.privilege.RolePriv;
import com.zving.platform.privilege.UserPriv;
import com.zving.platform.service.APIMethodService;

/**
 * 接口权限
 * 
 * @author 桑健
 * @mail sangj@zving.com
 * @date 2014-7-24
 */
@Alias("APIPriv")
public class APIPrivUI extends UIFacade {

	@Priv(RolePriv.MenuID + Privilege.Or + UserPriv.MenuID + Privilege.Or + BranchPriv.MenuID)
	public void init() {
		String id = $V("ID");
		String type = $V("Type");
		$S("PrivScript", PrivBL.getInitScript(type, id));
	}

	@Priv(RolePriv.MenuID + Privilege.Or + UserPriv.MenuID + Privilege.Or + BranchPriv.MenuID)
	public void bindAPIList(ListAction la) {
		final String id = $V("ID");
		if (StringUtil.isNull(id)) {
			return;
		}
		Mapx<String, PluginConfig> items = initItems();

		final String type = $V("Type");
		DataTable dt = new DataTable();
		dt.insertColumn("ID");
		dt.insertColumn("Name");
		dt.insertColumn("Checked");
		dt.insertColumn("Disabled");
		dt.insertColumn("APIPluginID");
		final Privilege p = PrivBL.getCurrentPrivilege(type, id);
		boolean fullPrivFlag = PrivBL.getFullPrivFlag(type, id);
		for (IAPIMethod item : APIMethodService.getInstance().getAll()) {
			String checked = "";
			String privID = APIUtil.Prefix + item.getExtendItemID();
			String apiPluginID = items.get(item.getClass().getName()).getID();

			// 如果type是用户U，并且所属机构没有该权限，并且item也没有该权限，则不显示该条接口
			if (Privilege.OwnerType_User.equals(type) && !PrivBL.isInBranchPrivRange(type, id, privID) && !p.hasPriv(privID)) {
				continue;
			}
			// 如果type是角色R，并且所属机构没有该权限，则不显示该条接口
			if (Privilege.OwnerType_Role.equals(type) && !PrivBL.isInBranchPrivRange(type, id, privID)) {
				continue;
			}

			if (!p.hasPriv(privID) && !PrivBL.isInBranchPrivRange(type, id, privID)) {
				dt.insertRow(new Object[] { privID, LangUtil.get(item.getExtendItemName()), checked, "disabled", apiPluginID });
				continue;
			}
			if (p != null && p.hasPriv(privID) || fullPrivFlag) {
				checked = "checked='true'";
			}

			dt.insertRow(new Object[] { privID, LangUtil.get(item.getExtendItemName()), checked, "", apiPluginID });
		}
		la.bindData(dt);
	}

	@Priv(RolePriv.MenuID + Privilege.Or + UserPriv.MenuID + Privilege.Or + BranchPriv.MenuID)
	public void bindPluginList(ListAction la) {
		final String id = $V("ID");
		if (StringUtil.isNull(id)) {
			return;
		}
		Mapx<String, PluginConfig> items = initItems();
		final String type = $V("Type");
		DataTable dt = new DataTable();
		dt.insertColumn("PluginID");
		dt.insertColumn("PluginName");
		dt.insertColumn("Checked");
		dt.insertColumn("isInBranch");
		final Privilege p = PrivBL.getCurrentPrivilege(type, id);
		Collection<PluginConfig> c = items.values();
		Set<PluginConfig> s = new HashSet<PluginConfig>();
		// 去除重复的PluginConfig
		s.addAll(c);
		for (PluginConfig pc : s) {
			String pid = pc.getID();
			String checked = p.hasPriv(pid) ? "checked='true'" : "";
			String isInBranch = PrivBL.isInBranchPrivRange(type, id, pid) ? "true" : "";
			dt.insertRow(new Object[] { pc.getID(), pc.getName(), checked, isInBranch });
		}
		la.bindData(dt);
	}

	/**
	 * 过滤一些没有接口服务的扩展项
	 * 
	 * @return 返回的Mapx的key为扩展项的class，value为PluginConfig
	 */
	private Mapx<String, PluginConfig> initItems() {
		List<PluginConfig> pcList = PluginManager.getInstance().getAllPluginConfig();
		Mapx<String, PluginConfig> map = new Mapx<String, PluginConfig>();
		// 遍历插件
		for (PluginConfig pc : pcList) {
			Mapx<String, ExtendItemConfig> m = pc.getExtendItems();
			Iterator<ExtendItemConfig> it = m.values().iterator();
			String apiServiceID = "com.zving.platform.service.APIMethodService";
			while (it.hasNext()) {
				ExtendItemConfig e = it.next();
				// 过滤一些没有接口服务的扩展项
				if (e.getExtendServiceID().equals(apiServiceID)) {
					map.put(e.getClassName(), pc);
				}
			}
		}
		return map;
	}

	@Priv(RolePriv.SetPriv + Privilege.Or + UserPriv.SetPriv + Privilege.Or + BranchPriv.SetPrivRange)
	public void save() {
		PrivBL.save(Request, Response);
	}
}
