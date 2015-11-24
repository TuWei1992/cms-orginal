package com.zving.platform.ui;

import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.Lang;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.privilege.UserPriv;

@Alias("UserSelect")
public class UserSelectUI extends UIFacade {

	@Priv
	public void init() {
	}

	@Priv
	public void selectedUserDataBind(DataGridAction dga) {
		String selectedUser = dga.getParam("SelectedUsers");
		if (StringUtil.isNotEmpty(selectedUser)) {
			Q q = new Q().select("a.*", "b.Name as BranchName").from("ZDUser a", "ZDBranch b").where()
					.eq2("a.BranchInnerCode", "b.BranchInnerCode");
			q.and().in("a.UserName", selectedUser);
			DataTable dt = q.fetch();
			dga.setTotal(dt.getRowCount());
			dga.bindData(dt);
		} else {
			dga.bindData(new DataTable());
			return;
		}
	}

	@Priv
	public void allUserDataBind(DataGridAction dga) {
		Q q = new Q().select("a.*", "b.Name as BranchName").from("ZDUser a", "ZDBranch b").where()
				.eq2("a.BranchInnerCode", "b.BranchInnerCode");
		String selectedUser = $V("SelectedUsers");
		String searchContent = $V("SearchContent");
		String branchInnerCode = User.getBranchInnerCode();
		if (StringUtil.isNotEmpty(selectedUser)) {
			q.and().not().in("a.UserName", selectedUser);
		}
		if (StringUtil.isNotEmpty($V("BranchInnerCode")) && $V("BranchInnerCode").startsWith(branchInnerCode)) {
			q.and().eq("a.BranchInnerCode", $V("BranchInnerCode"));// 只能搜索用户所属结构下的
		}
		if (StringUtil.isNotEmpty(searchContent)) {
			q.and().braceLeft().like("a.UserName", searchContent).or().like("a.RealName", searchContent).braceRight();
		}
		dga.bindData(q);
	}

	@Priv(UserPriv.MenuID)
	public void bindBranchTree(TreeAction ta) {
		Q qb = new Q().select("*").from("ZDBranch");
		qb.orderby("OrderFlag,BranchInnerCode");
		DataTable dt = qb.fetch();
		ta.setIdentifierColumnName("BranchInnerCode");
		ta.setParentIdentifierColumnName("ParentInnerCode");
		ta.setRootText(Lang.get("Platform.BranchList"));
		ta.bindData(dt);
	}

}
