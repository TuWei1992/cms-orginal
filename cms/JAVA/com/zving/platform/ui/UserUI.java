package com.zving.platform.ui;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Filter;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.json.JSON;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.security.PasswordUtil;
import com.zving.framework.security.PrivCheck;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.DataListAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.bl.BranchBL;
import com.zving.platform.bl.UserBL;
import com.zving.platform.code.Enable;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.config.AdminUserName;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.SystemModelType.SystemMetaModel;
import com.zving.platform.point.AfterUserAddAction;
import com.zving.platform.point.AfterUserDeleteAction;
import com.zving.platform.point.AfterUserModifyAction;
import com.zving.platform.privilege.UserPriv;
import com.zving.platform.service.MenuPrivService;
import com.zving.platform.service.ShortcutPreference;
import com.zving.platform.service.UserPreferencesService;
import com.zving.platform.util.PlatformCache;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZDBranch;
import com.zving.schema.ZDUser;
import com.zving.schema.ZDUserPreferences;

/**
 * @Author 黄雷
 * @Date 2007-8-6
 * @Mail huanglei@zving.com
 */
@Alias("User")
public class UserUI extends UIFacade {
	@Priv(UserPriv.MenuID)
	public void init() {
		String userName = $V("ID");
		$S("UserName", userName);// 这一行是因为权限扩展都统一传ID
		if (ObjectUtil.notEmpty(userName)) {
			ZDUser user = new ZDUser();
			user.setUserName(userName);
			user.fill();
			Response.putAll(user.toMapx());
			if (StringUtil.isNotNull(user.getBranchInnerCode())) {
				ZDBranch branch = PlatformCache.getBranch(user.getBranchInnerCode());
				if (!ObjectUtil.empty(branch)) {
					$S("BranchName", branch.getName());
				}
			}
			$S("Password", "********");
			Request.putAll(MetaUtil.getExtendData(user.getUserName(), SystemMetaModel.getValue(SystemMetaModel.User)));
		}
		$S("MetaModelControlHtml", BranchBL.getSystemMetaModel(SystemMetaModel.User));
	}

	@Priv(UserPriv.MenuID)
	public void bindGrid(DataGridAction dga) {
		String userName = dga.getParam("UserName");
		String realName = dga.getParam("RealName");
		String branchInnerCode = dga.getParam("BranchInnerCode");
		if (StringUtil.isEmpty(branchInnerCode)) {
			branchInnerCode = User.getBranchInnerCode();
		}
		Q q = new Q().select("*").from("ZDUser").where("1", 1);
		if (StringUtil.isNotEmpty(userName)) {
			q.and().likeRight("UserName", userName.trim());
		}
		if (StringUtil.isNotEmpty(realName)) {
			q.and().likeRight("RealName", realName.trim());
		}
		if (StringUtil.isNotEmpty(branchInnerCode)) {
			q.and().likeRight("BranchInnerCode", branchInnerCode.trim());
		}
		q.orderby("AddTime desc,UserName");
		dga.setTotal(q);
		DataTable dt = q.fetch(dga.getPageSize(), dga.getPageIndex());
		dt.decodeColumn("BranchInnerCode", new Q().select("BranchInnerCode", "Name").from("ZDBranch").fetch().toMapx(0, 1));
		dt.decodeColumn("Status", PlatformUtil.getCodeMap("Enable"));
		LangUtil.decode(dt, "BranchInnerCodeName");
		LangUtil.decode(dt, "RealName");
		dt.insertColumn("RoleNames");
		for (int i = 0; i < dt.getRowCount(); i++) {
			List<String> roles = PlatformUtil.getRoleCodesByUserName(dt.getString(i, "UserName"));
			if (ObjectUtil.notEmpty(roles)) {
				dt.set(i, "RoleNames", PlatformUtil.getRoleNames(roles));
			}
		}
		YesOrNo.decodeYesOrNoIcon(dt, "Status", false);
		dga.bindData(dt);
	}

	@Priv(UserPriv.MenuID)
	public void bindRoleTree(TreeAction ta) {
		Q q = new Q().select("RoleCode", "RoleName").from("ZDRole").where().likeRight("BranchInnerCode", User.getBranchInnerCode());
		DataTable dt = q.fetch();
		dt.insertColumn("ParentID", "");
		dt.insertColumn("TreeLevel", "1");
		dt.insertColumn("Checked", "");
		dt.insertColumn("ChkDisabled", "false");
		String userName = $V("ID");
		List<String> roles = null;
		if (ObjectUtil.notEmpty(userName)) {
			roles = PlatformUtil.getRoleCodesByUserName(userName);
		}
		for (DataRow dr : dt) {
			String code = dr.getString("RoleCode");
			if (StringUtil.isNotEmpty(userName) && userName.equals("admin") && code.equalsIgnoreCase("admin")) {
				dr.set("ChkDisabled", "true");// 如果当前用户为admin，则禁用对admin节点的勾选修改功能
			}
			if (ObjectUtil.empty(userName) && RoleUI.EVERYONE.equalsIgnoreCase(code)) {
				dr.set("Checked", true);
			}
			if (roles != null && roles.contains(code)) {
				dr.set("Checked", true);
			}
		}
		ta.setRootText(Lang.get("Platform.Plugin.Role"));
		ta.setIdentifierColumnName("RoleCode");
		LangUtil.decode(dt, "RoleName");
		ta.bindData(dt);
	}

	@Priv(UserPriv.Add)
	public void add() {
		if (new Q().select("count(*)").from("ZDUser").executeInt() >= LicenseInfo.getUserLimit()) {
			fail(Lang.get("Platform.LicenseWarning"));
			return;
		}
		Transaction trans = new Transaction();
		Object[] obj = UserBL.addUser(trans, Request);
		if (ObjectUtil.empty(obj)) {
			fail(Errorx.printString());
			return;
		}

		if (trans.commit()) {
			ExtendManager.invoke(AfterUserAddAction.ExtendPointID, obj);
			success(Lang.get("Common.AddSuccess"));
		} else {
			fail(Lang.get("Common.AddFailed"));
		}
	}

	@Priv(UserPriv.Edit)
	public void save() {
		Transaction trans = new Transaction();
		Object[] obj = UserBL.saveUser(trans, Request);
		if (ObjectUtil.empty(obj)) {
			fail(Errorx.getAllMessage());
			return;
		}

		if (trans.commit()) {
			ExtendManager.invoke(AfterUserModifyAction.ExtendPointID, obj);
			success(Lang.get("Common.ModifySuccess"));
		} else {
			fail(Lang.get("Common.ModifyFailed"));
		}
	}

	private static Pattern IDPattern = Pattern.compile("[\\w@\\.\\,\\_\\-]*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	@Priv(UserPriv.Delete)
	public void del() {
		String userNames = $V("UserNames");
		if (!IDPattern.matcher(userNames).matches()) {
			fail("Invalid UserNames:" + userNames);
			return;
		}
		Transaction trans = new Transaction();
		Object[] obj = UserBL.deleteUser(trans, Request);
		if (ObjectUtil.empty(obj)) {
			fail(Errorx.printString());
			return;
		}
		if (trans.commit()) {
			ExtendManager.invoke(AfterUserDeleteAction.ExtendPointID, obj);
			success(Lang.get("Common.DeleteSuccess"));
		} else {
			fail(Lang.get("Common.DeleteFailed"));
		}
	}

	@Priv(UserPriv.Disable)
	public void disableUser() {
		String UserNames = $V("UserNames");
		if (!IDPattern.matcher(UserNames).matches()) {
			fail("Invalid UserName!");
			return;
		}
		ZDUser user = new ZDUser();
		DAOSet<ZDUser> userSet = user.query(new Q().where().in("UserName", UserNames));
		for (int i = 0; i < userSet.size(); i++) {
			if (AdminUserName.getValue().equalsIgnoreCase(userSet.get(i).getUserName())) {
				fail(AdminUserName.getValue() + Lang.get("User.CannotDisableAdmin"));
				return;
			}
			userSet.get(i).setStatus(Enable.Disable);
		}
		if (userSet.update()) {
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	@Priv(UserPriv.Enable)
	public void enableUser() {
		String userNames = $V("UserNames");
		if (!IDPattern.matcher(userNames).matches()) {
			fail("Invalid UserName!");
			return;
		}
		ZDUser user = new ZDUser();
		DAOSet<ZDUser> userSet = user.query(new Q().where().in("UserName", userNames));
		for (int i = 0; i < userSet.size(); i++) {
			if (AdminUserName.getValue().equalsIgnoreCase(userSet.get(i).getUserName())) {
				fail(AdminUserName.getValue() + Lang.get("User.CannotDisableAdmin"));
				return;
			}
			userSet.get(i).setStatus(Enable.Enable);
		}
		if (userSet.update()) {
			success(Lang.get("Common.ExecuteSuccess"));
		} else {
			fail(Lang.get("Common.ExecuteFailed"));
		}
	}

	@Priv(UserPriv.ChangePassword)
	public void changePassword() {
		String Password = $V("Password");
		Q q = new Q().update("ZDUser").set("Password", PasswordUtil.generate(Password));
		q.where("UserName", $V("UserName"));
		if (q.executeNoQuery() > 0) {
			success(Lang.get("Common.ModifySuccess"));
		} else {
			fail(Lang.get("Common.ModifyFailed"));
		}
	}

	@Priv
	public void initUserPreferences() {
		Response.putAll(UserPreferencesService.getUerPreferences(User.getUserName()));
	}

	@Priv
	public void saveUserPreferences() {
		if (!UserPreferencesService.validate(Request)) {
			fail(Lang.get("Common.InvalidID"));
			return;
		}
		ZDUserPreferences up = new ZDUserPreferences();
		up.setUserName(User.getUserName());
		if (up.fill()) {
			up.setModifyTime(new Date());
			up.setModifyUser(User.getUserName());
			Current.getTransaction().update(up);
		} else {
			up.setAddTime(new Date());
			up.setAddUser(User.getUserName());
			Current.getTransaction().insert(up);
		}
		Mapx<String, String> map = UserPreferencesService.process(Request);
		up.setConfigProps(JSON.toJSONString(map));
		if (Current.getTransaction().commit()) {
			User.getCurrent().putAll(map);
			success(Lang.get("Common.SaveSuccess"));
		} else {
			fail(Lang.get("Common.SaveFailed"));
		}
	}

	@Priv
	public void bindShortcut(DataListAction dla) {
		String menuids = $V("MenuIDs");
		if (StringUtil.isNull(menuids) && !YesOrNo.isYes($V("Edit"))) {
			menuids = User.getValue(ShortcutPreference.ID) + "";
		}
		final String menus = "," + menuids + ",";
		DataTable dt = MenuPrivService.getAllMenus(false);
		dt = dt.filter(new Filter<DataRow>() {
			@Override
			public boolean filter(DataRow dr) {
				if (StringUtil.isEmpty(dr.getString("URL"))) {
					return false;
				}
				return menus.indexOf("," + dr.getString("ID") + ",") >= 0 && PrivCheck.check(dr.getString("ID"));
			}
		});
		dt.insertColumn("_RowNo");
		for (int i = 1; i <= dt.getRowCount(); i++) {
			dt.get(i - 1).set("_RowNo", i);
		}
		dla.bindData(dt.getPagedDataTable(8, 0));
	}
}
