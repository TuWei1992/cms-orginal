package com.zving.platform.ui;

import java.util.Date;

import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.Privilege;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.TreeAction;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.bl.BranchBL;
import com.zving.platform.bl.RoleBL;
import com.zving.platform.point.AfterRoleAddAction;
import com.zving.platform.point.AfterRoleDeleteAction;
import com.zving.platform.point.AfterRoleModifyAction;
import com.zving.platform.privilege.RolePriv;
import com.zving.platform.util.PlatformCache;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZDPrivilege;
import com.zving.schema.ZDRole;
import com.zving.schema.ZDUserRole;

/**
 * @Author 黄雷
 * @Date 2007-8-6
 * @Mail huanglei@zving.com
 */
@Alias("Role")
public class RoleUI extends UIFacade {
	public final static String EVERYONE = "everyone";

	@Priv(RolePriv.MenuID)
	public void bindBranchTree(TreeAction ta) {
		DataTable dt = BranchBL.loadTreeTable(Request.getString("ParentID")); // 文档工作台加载栏目树时，根据栏目下的文档权限控制栏目是否显示
		dt.insertColumn("Icon");
		for (DataRow dr : dt) {
			dr.set("Icon", "icons/icon042a1.png");
		}
		ta.setRootText(Lang.get("Platform.BranchList"));
		ta.setIdentifierColumnName("ID");
		ta.bindData(dt);
	}

	@Priv(RolePriv.MenuID)
	public void bindTree(TreeAction ta) {
		String branchInnerCode = Request.getString("BranchInnerCode");
		Q q = new Q().select("RoleCode", "RoleName").from("ZDRole");
		if (!User.isBranchAdministrator()) {
			if (StringUtil.isEmpty(branchInnerCode)) {
				q.where().likeRight("BranchInnerCode", User.getBranchInnerCode());
			} else {
				q.where().likeRight("BranchInnerCode", branchInnerCode);
			}
		} else {
			if (StringUtil.isNotEmpty(branchInnerCode)) {
				q.where().likeRight("BranchInnerCode", branchInnerCode);
			}
		}
		q.orderby("BranchInnerCode,AddTime");
		DataTable dt = q.fetch();
		dt.insertColumn("TreeLevel", "1");
		LangUtil.decode(dt, "RoleName");
		ta.setRootText(Lang.get("Role.Title"));
		ta.setIdentifierColumnName("RoleCode");
		ta.setBranchIcon("icons/icon025a1.png");
		ta.setLeafIcon("icons/icon025a1.png");
		ta.bindData(dt);
	}

	/**
	 * 初始化用户新建短消息时选择角色列表
	 * 
	 * @param dga
	 */
	@Priv
	public void roleDataBind(DataGridAction dga) {
		DataTable dt = new Q().select("RoleCode", "RoleName").from("ZDRole").fetch();
		LangUtil.decode(dt, "RoleName");
		dga.bindData(dt);
	}

	/**
	 * 初始化获取角色信息
	 */
	@Priv(RolePriv.MenuID)
	public void init() {
		String RoleCode = $V("ID");
		String branchInnerCode = $V("BranchInnerCode");
		if (StringUtil.isNotEmpty(branchInnerCode)) {
			String name = new Q().select("Name").from("ZDBranch").where("BranchInnerCode", branchInnerCode).executeString();
			$S("BranchInnerCode", branchInnerCode);
			$S("BranchName", name);
		}
		$S("RoleCode", RoleCode);// 这一行是因为权限扩展都统一传ID
		if (ObjectUtil.notEmpty(RoleCode)) {
			ZDRole role = new ZDRole();
			role.setRoleCode(RoleCode);
			if (role.fill()) {
				Mapx<String, Object> map = role.toMapx();
				Response.putAll(map);

				String name = new Q().select("Name").from("ZDBranch").where("BranchInnerCode", role.getBranchInnerCode()).executeString();
				$S("BranchName", name);
				LangUtil.decode(Response);
			}
		}
	}

	/**
	 * 添加角色
	 */
	@Priv(RolePriv.Add)
	public void add() {
		ZDRole role = new ZDRole();
		role.setValue(Request);
		role.setRoleCode(role.getRoleCode().toLowerCase());
		if (role.fill()) {
			fail(Lang.get("Role.Code") + role.getRoleCode() + " " + Lang.get("Common.Exists"));
			return;
		}
		Date currentDate = new Date();
		String currentUserName = User.getUserName();
		role.setAddTime(currentDate);
		role.setAddUser(currentUserName);
		role.setRoleName(LangUtil.getI18nFieldValue("RoleName"));
		role.setMemo(LangUtil.getI18nFieldValue("Memo"));

		Transaction tran = new Transaction();
		tran.add(role, Transaction.INSERT);

		ZDPrivilege priv = new ZDPrivilege();
		priv.setOwnerType(Privilege.OwnerType_Role);
		priv.setOwner(role.getRoleCode());
		priv.setAddTime(currentDate);
		priv.setAddUser(currentUserName);
		tran.add(priv, Transaction.INSERT);

		if (tran.commit()) {
			CacheManager.set(PlatformCache.ProviderID, "Role", role.getRoleCode(), role);
			ExtendManager.invoke(AfterRoleAddAction.ExtendPointID, new Object[] { role, priv });
			success(Lang.get("Common.AddSuccess"));
		} else {
			fail(Lang.get("Common.AddFailed"));
		}
	}

	/**
	 * 保存修改信息
	 */
	@Priv(RolePriv.Edit)
	public void save() {
		ZDRole role = new ZDRole();
		role.setRoleCode($V("RoleCode"));
		role.fill();

		role.setValue(Request);
		role.setModifyTime(new Date());
		role.setModifyUser(User.getUserName());
		role.setRoleName(LangUtil.getI18nFieldValue("RoleName"));
		role.setMemo(LangUtil.getI18nFieldValue("Memo"));

		if (role.update()) {
			CacheManager.set(PlatformCache.ProviderID, "Role", role.getRoleCode(), role);
			ExtendManager.invoke(AfterRoleModifyAction.ExtendPointID, new Object[] { role });
			success(Lang.get("Common.ModifySuccess"));
		} else {
			fail(Lang.get("Common.ModifyFailed"));
		}
	}

	/**
	 * 删除角色时需要做的步骤 更新机构中的角色数 删除用户与角色的关系 删除这个角色的所有权限记录 更新这个角色下的所有用户的权限记录
	 */
	@Priv(RolePriv.Delete)
	public void del() {
		String roleCode = Request.getString("RoleCode");
		Transaction tran = new Transaction();
		ZDRole role = new ZDRole();
		role.setRoleCode(roleCode);
		role.fill();
		if (EVERYONE.equalsIgnoreCase(roleCode)) {
			fail(EVERYONE + " " + Lang.get("Role.CannotDelete"));
			return;
		}
		if (RoleBL.getAdminRoleCode().equalsIgnoreCase(roleCode)) {
			fail(RoleBL.getAdminRoleCode() + " " + Lang.get("Role.CannotDelete"));
			return;
		}
		// 删除角色
		tran.add(role, Transaction.DELETE_AND_BACKUP);

		ZDUserRole userRole = new ZDUserRole();
		DAOSet<ZDUserRole> userRoleSet = userRole.query(new Q("where RoleCode =?", roleCode));
		// 删除角色与用户的关系
		tran.add(userRoleSet, Transaction.DELETE_AND_BACKUP);

		// 删除角色的权限
		DAOSet<ZDPrivilege> privilege = new ZDPrivilege().query(new Q().where("OwnerType", Privilege.OwnerType_Role).and()
				.eq("Owner", roleCode));
		tran.add(privilege, Transaction.DELETE_AND_BACKUP);

		if (tran.commit()) {
			PlatformCache.removeRole(role.getRoleCode());
			ExtendManager.invoke(AfterRoleDeleteAction.ExtendPointID, new Object[] { role, userRoleSet, privilege });
			success(Lang.get("Common.DeleteSuccess"));
		} else {
			fail(Lang.get("Common.DeleteFailed"));
		}
	}

	/**
	 * 显示选中角色下的所有用户信息
	 */
	@Priv(RolePriv.MenuID)
	public void bindGrid(DataGridAction dga) {
		String roleCode = dga.getParam("ID");
		if (roleCode == null || "".equals(roleCode)) {
			roleCode = dga.getParams().getString("Cookie.Role.LastRoleCode");
			if (roleCode == null || "".equals(roleCode)) {
				dga.bindData(new DataTable());
				return;
			}
		}
		Q q = new Q().select("*").from("ZDUser a").where().exists().braceLeft().select("UserName").from("ZDUserRole b").where()
				.eq2("b.UserName", "a.UserName").and().eq("b.RoleCode", roleCode).braceRight();
		DataTable dt = q.fetch(dga.getPageSize(), dga.getPageIndex());
		dt.insertColumn("RoleNames");
		for (int i = 0; i < dt.getRowCount(); i++) {
			dt.set(i, "RoleNames", PlatformUtil.getRoleNames(PlatformUtil.getRoleCodesByUserName(dt.getString(i, "UserName"))));
		}
		LangUtil.decode(dt, "RealName");
		dga.setTotal(q);
		dga.bindData(dt);
	}

	/**
	 * 获取该角色下的所有用户
	 */
	@Priv(RolePriv.MenuID)
	public void bindUserList(DataGridAction dga) {
		String roleCode = dga.getParam("RoleCode");
		String searchUserName = dga.getParam("SearchUserName");
		if (StringUtil.isNotEmpty(searchUserName)) {
			searchUserName = searchUserName.trim();
		}
		Q q = new Q().select("*").from("ZDUser a").where().likeRight("BranchInnerCode", User.getBranchInnerCode());
		q.and().not().exists().braceLeft().select("''").from("ZDUserRole b").where("b.roleCode", roleCode).and()
				.eq2("b.userName", "a.userName").braceRight();
		if (StringUtil.isNotEmpty(searchUserName)) {
			q.and().braceLeft().like("UserName", searchUserName);
			// 查询真实姓名
			q.or().like("RealName", searchUserName).braceRight();
		}
		dga.setTotal(q);
		DataTable dt = q.fetch(dga.getPageSize(), dga.getPageIndex());
		dt.decodeColumn("Status", PlatformUtil.getCodeMap("Enable"));
		LangUtil.decode(dt, "StatusName");
		LangUtil.decode(dt, "RealName");
		dga.bindData(dt);
	}

	/**
	 * 添加用户到一个角色中
	 */
	@Priv(RolePriv.AddUser)
	public void addUserToRole() {
		String roleCode = $V("RoleCode");
		if (StringUtil.isEmpty(roleCode)) {
			return;
		}
		String[] userNames = $V("UserNames").split(",");
		Date currentDate = new Date();
		String currentUserName = User.getUserName();
		Transaction tran = new Transaction();

		DAOSet<ZDUserRole> set = new DAOSet<ZDUserRole>();
		for (String userName : userNames) {
			if (StringUtil.isEmpty(userName)) {
				continue;
			}
			ZDUserRole userRole = new ZDUserRole();
			userRole.setUserName(userName);
			userRole.setRoleCode(roleCode);
			userRole.setAddTime(currentDate);
			userRole.setAddUser(currentUserName);
			set.add(userRole);
		}
		tran.add(set, Transaction.INSERT);
		if (tran.commit()) {
			for (int i = 0; i < set.size(); i++) {
				PlatformCache.addUserRole(set.get(i).getUserName(), set.get(i).getRoleCode());
			}
			success(Lang.get("Common.AddSuccess"));
		} else {
			fail(Lang.get("Common.AddFailed"));
		}
	}

	/**
	 * 从角色中删除用户
	 */
	@Priv(RolePriv.RemoveUser)
	public void delUserFromRole() {
		String roleCode = $V("RoleCode");
		String[] userNames = $V("UserNames").split(",");
		Transaction tran = new Transaction();

		DAOSet<ZDUserRole> set = new DAOSet<ZDUserRole>();
		for (String userName : userNames) {
			DataTable dt = new Q().select("RoleCode").from("ZDUserRole").where("UserName", userName).and().ne("RoleCode", roleCode).fetch();
			String[] roleCodes = new String[dt.getRowCount()];
			for (int j = 0; j < dt.getRowCount(); j++) {
				roleCodes[j] = dt.getString(j, 0);
			}
			ZDUserRole userRole = new ZDUserRole();
			userRole.setUserName(userName);
			userRole.setRoleCode(roleCode);
			userRole.fill();
			set.add(userRole);
		}
		tran.add(set, Transaction.DELETE_AND_BACKUP);
		if (tran.commit()) {
			for (int i = 0; i < set.size(); i++) {
				PlatformCache.removeUserRole(set.get(i).getUserName(), set.get(i).getRoleCode());
			}
			success(Lang.get("Common.DeleteSuccess"));
		} else {
			fail(Lang.get("Common.DeleteFailed"));
		}
	}

}
