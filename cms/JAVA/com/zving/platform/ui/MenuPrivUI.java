package com.zving.platform.ui;

import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Filter;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.security.Privilege;
import com.zving.framework.ui.tag.ListAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.bl.PrivBL;
import com.zving.platform.privilege.AbstractMenuPriv;
import com.zving.platform.privilege.BranchPriv;
import com.zving.platform.privilege.RolePriv;
import com.zving.platform.privilege.UserPriv;
import com.zving.platform.service.MenuPrivService;

/**
 * @Author 王育春
 * @Date 2012-1-6
 * @Mail wyuch@zving.com
 */
@Alias("MenuPriv")
public class MenuPrivUI extends UIFacade {
	public static final String PrivType = "Menu";

	@Priv(RolePriv.MenuID + Privilege.Or + UserPriv.MenuID + Privilege.Or + BranchPriv.MenuID)
	public void init() {
		String id = $V("ID");
		String type = $V("Type");
		$S("PrivScript", PrivBL.getInitScript(type, id));
	}

	@Priv(RolePriv.MenuID + Privilege.Or + UserPriv.MenuID + Privilege.Or + BranchPriv.MenuID)
	public void bindMenuList(ListAction la) {
		final String id = $V("ID");
		final String type = $V("Type");

		DataTable dt = MenuPrivService.getAllMenus(false);
		if (StringUtil.isNull(id)) {
			return;
		}
		final Privilege p = PrivBL.getCurrentPrivilege(type, id);

		dt = dt.filter(new Filter<DataRow>() {
			@Override
			public boolean filter(DataRow dr) {

				String privID = dr.getString("ID");
				return p.hasPriv(privID) || PrivBL.isInBranchPrivRange(type, id, privID);
			}
		});

		dt.insertColumn("Checked");
		dt.insertColumn("PrivMemo");
		dt.insertColumn("ParentFlag");
		for (DataRow dr : dt) {
			String menuID = dr.getString("ID");
			if (p.hasPriv(menuID) || PrivBL.getFullPrivFlag(type, id)) {
				dr.set("Checked", "checked='true'");
			}
			String parentMenuID = dr.getString("ParentID");
			for (DataRow dr2 : dt) {
				if (dr2.getString("ID").equals(parentMenuID)) {
					dr.set("ParentFlag", "Y");
					break;
				}
			}
			AbstractMenuPriv ap = MenuPrivService.getInstance().get(menuID);
			if (ap != null && ObjectUtil.notEmpty(ap.getMemo())) {
				dr.set("PrivMemo", LangUtil.get(ap.getMemo()));
			}
		}
		la.bindData(dt);
	}

	@Priv(RolePriv.MenuID + Privilege.Or + UserPriv.MenuID + Privilege.Or + BranchPriv.MenuID)
	public void bindPrivItemList(ListAction la) {
		final String id = $V("ID");
		final String type = $V("Type");

		DataTable dt = new DataTable();
		dt.insertColumn("Item");
		dt.insertColumn("Name");
		dt.insertColumn("Checked");
		String menuID = la.getParentCurrentDataRow().getString("ID");
		AbstractMenuPriv ap = MenuPrivService.getInstance().get(menuID);
		if (ap == null) {
			return;
		}
		Privilege p = PrivBL.getCurrentPrivilege(type, id);

		Mapx<String, String> items = ap.getPrivItems();
		for (String item : items.keySet()) {
			String checked = "";
			if (!p.hasPriv(item) && !PrivBL.isInBranchPrivRange(type, id, item)) {
				continue;
			}
			if (p.hasPriv(item) || PrivBL.getFullPrivFlag(type, id)) {
				checked = "checked='true'";
			}
			dt.insertRow(new Object[] { item, LangUtil.get(items.get(item)), checked });
		}
		la.bindData(dt);
	}

	@Priv(RolePriv.SetPriv + Privilege.Or + UserPriv.SetPriv + Privilege.Or + BranchPriv.SetPrivRange)
	public void save() {
		PrivBL.save(Request, Response);
	}
}
