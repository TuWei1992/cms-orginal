package com.zving.platform.bl;

import java.util.Date;
import java.util.regex.Pattern;

import com.zving.framework.Current;
import com.zving.framework.User;
import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataCollection;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.security.PasswordUtil;
import com.zving.framework.security.Privilege;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.Enable;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.config.AdminUserName;
import com.zving.platform.log.type.UserLog;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.SystemModelType.SystemMetaModel;
import com.zving.platform.point.AfterLoginAction;
import com.zving.platform.service.UserPreferencesService;
import com.zving.platform.util.PlatformCache;
import com.zving.schema.ZDPrivilege;
import com.zving.schema.ZDRole;
import com.zving.schema.ZDUser;
import com.zving.schema.ZDUserLog;
import com.zving.schema.ZDUserRole;

public class UserBL {

	public static Pattern UserPattern = Pattern.compile("[\\w@\\.\u4e00-\u9fa5]{1,20}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/**
	 * 删除一个用户需要做的步骤： 更新机构中的用户数 删除用户与角色的关系 更新角色中的用户数 删除这个用户的所有权限记录
	 */
	public static Object[] deleteUser(Transaction trans, DataCollection dc) {
		String userNames = dc.getString("UserNames");
		ZDUser user = new ZDUser();
		DAOSet<ZDUser> userSet = user.query(new Q().where().in("UserName", userNames));
		trans.add(userSet, Transaction.DELETE_AND_BACKUP);
		DAOSet<ZDUserRole> userRoleSetAll = new DAOSet<ZDUserRole>();
		DAOSet<ZDPrivilege> privAll = new DAOSet<ZDPrivilege>();
		for (int i = 0; i < userSet.size(); i++) {
			user = userSet.get(i);
			if (User.getUserName().equals(user.getUserName())) {
				Errorx.addError(Lang.get("User.CanDeleteSelf"));
				return null;
			}
			if (AdminUserName.getValue().equalsIgnoreCase(user.getUserName())) {
				Errorx.addError(AdminUserName.getValue() + Lang.get("User.CannotDeleteAdmin"));
				return null;
			}

			CacheManager.remove(PlatformCache.ProviderID, "User", user.getUserName());
			CacheManager.remove(PlatformCache.ProviderID, "UserRole", user.getUserName());

			// 删除并备份用户与机构的关系
			ZDUserRole userRole = new ZDUserRole();
			userRole.setUserName(user.getUserName());
			DAOSet<ZDUserRole> userRoleSet = userRole.query();
			userRoleSetAll.addAll(userRoleSet);
			trans.add(userRoleSet, Transaction.DELETE_AND_BACKUP);
			// 删除用户的权限

			DAOSet<ZDPrivilege> priv = new ZDPrivilege().query(new Q().where("OwnerType", Privilege.OwnerType_User).and()
					.eq("Owner", user.getUserName()));
			privAll.addAll(priv);
			trans.add(priv, Transaction.DELETE_AND_BACKUP);
			// 删除并备份用户的操作日志
			DAOSet<ZDUserLog> logSet = new ZDUserLog().query(new Q().where("UserName", user.getUserName()));
			trans.add(logSet, Transaction.DELETE_AND_BACKUP);
		}
		return new Object[] { userSet, userRoleSetAll, privAll };
	}

	public static Object[] saveUser(Transaction trans, DataCollection dc) {
		ZDUser user = new ZDUser();
		user.setUserName(dc.getString("UserName"));
		if (!user.fill()) {
			Errorx.addError(Lang.get("Common.UserName") + " " + user.getUserName() + " " + Lang.get("Common.NotFound"));
			return null;
		}
		String oldPassword = user.getPassword();
		String oldBranch = user.getBranchInnerCode();
		user.setValue(dc);
		if (AdminUserName.getValue().equalsIgnoreCase(user.getUserName()) && Enable.isDisable(user.getStatus())) {
			Errorx.addError(AdminUserName.getValue() + " " + Lang.get("User.CannotDisableAdmin"));
			return null;
		}
		user.setModifyTime(new Date());
		user.setModifyUser(User.getUserName());
		user.setRealName(LangUtil.getI18nFieldValue("RealName"));
		user.setPassword(oldPassword);// 此处得主要是防止没有修改密码按钮权限的用户可以修改用户的密码
		if (StringUtil.isNull(user.getStatus())) {
			user.setStatus(YesOrNo.Yes); // 若未设置状态则默认为启用状态
		}
		MetaUtil.saveExtendData(dc, user.getUserName(), SystemMetaModel.getValue(SystemMetaModel.User));
		trans.add(user, Transaction.UPDATE);

		CacheManager.set(PlatformCache.ProviderID, "User", user.getUserName(), user);

		// 角色
		ZDUserRole userRole = new ZDUserRole();
		userRole.setUserName(user.getUserName());
		DAOSet<ZDUserRole> userRoleSet = userRole.query();
		trans.add(userRoleSet, Transaction.DELETE_AND_BACKUP);
		CacheManager.set(PlatformCache.ProviderID, "UserRole", user.getUserName(), "");

		if (StringUtil.isNotEmpty(user.getBranchInnerCode()) && !user.getBranchInnerCode().equals(oldBranch)) {
			// 用户机构改变时更新用户权限,取新机构权限和用户权限的交集
			ZDPrivilege privilege = new ZDPrivilege();
			privilege.setOwnerType(Privilege.OwnerType_User);
			privilege.setOwner(user.getUserName());
			if (privilege.fill()) {
				Privilege bp = PrivBL.getBranchPriv(user.getBranchInnerCode());
				Privilege p = new Privilege();
				String privs = privilege.getPrivs();
				p.parse(privs);
				if (!PrivBL.getFullPrivFlag(Privilege.OwnerType_Branch, user.getBranchInnerCode())) {
					p.intersect(bp);
				}
				privilege.setPrivs(p.toString());
				trans.update(privilege);
			}
		}
		String roleCodes = dc.getString("RoleCode");
		if (StringUtil.isEmpty(roleCodes)) {
			return new Object[] { user, userRoleSet, "" };
		}

		String currentUserName = User.getUserName();

		// 用户所属机构修改时 应该去掉上一个所属机构的角色权限
		if (StringUtil.isNotNull(oldBranch) && !oldBranch.equals(user.getBranchInnerCode()) && StringUtil.isNotNull(roleCodes)) {
			String[] codes = roleCodes.split(",");
			StringBuilder code = new StringBuilder();
			for (String s : codes) {
				code.append("'");
				code.append(s);
				code.append("'");
				code.append(",");
			}
			DAOSet<ZDRole> roleSet = new ZDRole().query(new Q().where().in("RoleCode", code.substring(0, code.length() - 1)));
			roleCodes = "";
			for (ZDRole role : roleSet) {
				if (oldBranch.equals(role.getBranchInnerCode())) {
					continue;
				}
				roleCodes = roleCodes + role.getRoleCode() + ",";
			}
		}
		String[] RoleCodes = roleCodes.split(",");
		for (String roleCode : RoleCodes) {
			if (StringUtil.isEmpty(roleCode) || StringUtil.isEmpty(user.getUserName())) {
				continue;
			}
			userRole = new ZDUserRole();
			userRole.setUserName(user.getUserName());
			userRole.setRoleCode(roleCode);
			userRole.setAddTime(new Date());
			userRole.setAddUser(currentUserName);
			trans.add(userRole, Transaction.INSERT);

		}
		CacheManager.set(PlatformCache.ProviderID, "UserRole", user.getUserName(), roleCodes);
		return new Object[] { user, userRoleSet, roleCodes };
	}

	// 供外部调用
	public static Object[] addUser(Transaction trans, DataCollection dc) {
		String userName = dc.getString("UserName");
		if (!UserPattern.matcher(userName).matches()) {
			Errorx.addError(Lang.get("User.UserNameVerify"));
			return null;
		}
		ZDUser user = new ZDUser();
		user.setValue(dc);
		user.setUserName(user.getUserName().toLowerCase());
		if (user.fill()) {
			Errorx.addError(dc.getString("UserName") + Lang.get("Common.Exists"));
			return null;
		}

		user.setPassword(PasswordUtil.generate(dc.getString("Password")));
		user.setType(dc.getString("Type"));
		user.setProp1(dc.getString("Prop1"));
		user.setProp2(dc.getString("Prop2"));
		user.setProp3(dc.getString("Prop3"));
		user.setProp4(dc.getString("Prop4"));
		user.setAddTime(new Date());
		user.setAddUser(User.getUserName());
		user.setRealName(LangUtil.getI18nFieldValue("RealName"));
		if (StringUtil.isNull(user.getStatus())) {
			user.setStatus(YesOrNo.Yes); // 新建用户默认为启用状态
		}
		MetaUtil.saveExtendData(dc, user.getUserName(), SystemMetaModel.getValue(SystemMetaModel.User));
		trans.add(user, Transaction.INSERT);

		ZDPrivilege priv = new ZDPrivilege();
		priv.setOwnerType(Privilege.OwnerType_User);
		priv.setOwner(user.getUserName());
		priv.setAddTime(user.getAddTime());
		priv.setAddUser(user.getAddUser());
		trans.add(priv, Transaction.INSERT);

		// 角色
		String roleCodes = dc.getString("RoleCode");
		if (StringUtil.isEmpty(roleCodes)) {
			return new Object[] { user, priv, new DAOSet<ZDUserRole>() };
		}
		String[] RoleCodes = roleCodes.split(",");
		String currentUserName = User.getUserName();

		CacheManager.set(PlatformCache.ProviderID, "User", user.getUserName(), user);
		CacheManager.set(PlatformCache.ProviderID, "UserRole", user.getUserName(), roleCodes);
		DAOSet<ZDUserRole> userRoleSet = new DAOSet<ZDUserRole>();
		for (String roleCode : RoleCodes) {
			if (StringUtil.isEmpty(roleCode) || StringUtil.isEmpty(user.getUserName())) {
				continue;
			}
			ZDUserRole userRole = new ZDUserRole();
			userRole.setUserName(user.getUserName());
			userRole.setRoleCode(roleCode);
			userRole.setAddTime(new Date());
			userRole.setAddUser(currentUserName);
			userRoleSet.add(userRole);
			trans.add(userRole, Transaction.INSERT);
		}
		return new Object[] { user, priv, userRoleSet };
	}
	public static void login(ZDUser user) {
		login(user, false);
	}
	public static void login(ZDUser user, boolean isLdap) {
		String language = User.getLanguage();
		User.destory();
		User.setLanguage(language);
		User.setUserName(user.getUserName());
		User.setRealName(user.getRealName());
		User.setBranchInnerCode(user.getBranchInnerCode());
		User.setBranchAdministrator(YesOrNo.isYes(user.getIsBranchAdmin()));
		User.setType(user.getType());

		Mapx<String, Object> map = user.toMapx();
		map.remove("Password");
		User.getCurrent().putAll(map);
		User.getCurrent().putAll(UserPreferencesService.getUerPreferences(user.getUserName()));

		User.setLogin(true);
		User.setPrivilege(PrivBL.getUserPriv(User.getUserName()));

		user.setLastLoginIP(Current.getRequest().getClientIP());
		user.setLastLoginTime(new Date());
		user.update();
		// 用户登录记录日志
		LogBL.addLog(UserLog.ID, UserLog.SubType_Login, "User Login" + (isLdap?"(LDAP)":"") + Current.getRequest().getHeaders().getString("User-Agent"));

		ExtendManager.invoke(AfterLoginAction.ID, new Object[] { user });
	}
}
