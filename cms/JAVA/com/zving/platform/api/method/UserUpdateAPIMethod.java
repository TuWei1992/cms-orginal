package com.zving.platform.api.method;

import java.util.Date;

import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.AbstractAPIMethod;
import com.zving.platform.api.APIRequest;
import com.zving.platform.api.APIResponse;
import com.zving.platform.code.Enable;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.config.AdminUserName;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.SystemModelType.SystemMetaModel;
import com.zving.platform.util.PlatformCache;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZDRole;
import com.zving.schema.ZDUser;
import com.zving.schema.ZDUserRole;

public class UserUpdateAPIMethod extends AbstractAPIMethod {
	public static final String ID = "updateuser";

	public UserUpdateAPIMethod() {
		addOutput("", "Mapx", DataTypes.OBJECT, false, "@{Platform.API.KeyValue}");
		addOutput("Mapx", "userrolecode", DataTypes.STRING, true, "@{Platform.API.RoleCode}");
		addOutput("Mapx", "userrolename", DataTypes.STRING, true, "@{Platform.API.RoleName}");

		addParam("modifyuser", "@{Platform.ModifyUser}", DataTypes.STRING, false);
		addParam("username", "@{Common.UserName}", DataTypes.STRING, false);
		addParam("branchinnercode", "@{Platform.BranchInnercode}", DataTypes.STRING, false);
		addParam("email", "@{User.Email}", DataTypes.STRING, false);
		addParam("rolecode", "@{Platform.RoleCode}", DataTypes.STRING, false);
	}

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.API.UserUpdate}";
	}

	@Override
	public void execute(APIRequest request, APIResponse response) {
		Mapx<String, Object> params = request.getParameters();
		params.remove("password");// 避免数据接口密码导致用户密码被修改
		String modifyUser = params.getString("modifyuser");
		if (StringUtil.isEmpty(modifyUser)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：modifyuser");
			return;
		}
		String userName = params.getString("username");
		if (StringUtil.isEmpty(userName)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：username");
			return;
		}
		if (params.containsKey("branchinnercode")) {
			String branchInnerCode = params.getString("branchinnercode");
			if (StringUtil.isEmpty(branchInnerCode)) {
				response.setStatus(STATUS_FAILED);
				response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：branchinnercode");
				return;
			}
		}
		if (params.containsKey("email")) {
			String email = params.getString("email");
			if (StringUtil.isEmpty(email)) {
				response.setStatus(STATUS_FAILED);
				response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：email");
				return;
			}
		}
		Transaction trans = new Transaction();
		ZDUser user = new ZDUser();
		user.setUserName(userName);
		if (!user.fill()) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Common.UserName") + " " + user.getUserName() + " " + Lang.get("Common.NotFound"));
			return;
		}
		String oldPassword = user.getPassword();
		String oldBranch = user.getBranchInnerCode();
		user.setValue(params);
		if (AdminUserName.getValue().equalsIgnoreCase(user.getUserName()) && Enable.isDisable(user.getStatus())) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(AdminUserName.getValue() + " " + Lang.get("User.CannotDisableAdmin"));
			return;
		}
		user.setModifyTime(new Date());
		user.setModifyUser(modifyUser);
		user.setRealName(LangUtil.getI18nFieldValue("RealName"));
		user.setPassword(oldPassword);// 此处得主要是防止没有修改密码按钮权限的用户可以修改用户的密码
		if (StringUtil.isNull(user.getStatus())) {
			user.setStatus(YesOrNo.Yes); // 若未设置状态则默认为启用状态
		}
		MetaUtil.saveExtendData(params, user.getUserName(), SystemMetaModel.getValue(SystemMetaModel.User));
		trans.add(user, Transaction.UPDATE);

		CacheManager.set(PlatformCache.ProviderID, "User", user.getUserName(), user);

		// 角色
		ZDUserRole userRole = new ZDUserRole();
		userRole.setUserName(user.getUserName());
		trans.add(userRole.query(), Transaction.DELETE_AND_BACKUP);
		CacheManager.set(PlatformCache.ProviderID, "UserRole", user.getUserName(), "");

		String roleCodes = params.getString("rolecode");
		if (StringUtil.isNotEmpty(roleCodes)) {
			// 用户所属机构修改时 应该去掉上一个所属机构的角色权限
			if (StringUtil.isNotNull(oldBranch) && !oldBranch.equals(user.getBranchInnerCode()) && StringUtil.isNotNull(roleCodes)) {
				String[] codes = roleCodes.split(",");
				DAOSet<ZDRole> roleSet = new ZDRole().query(new Q().where().in("RoleCode", codes));
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
				userRole.setAddUser(modifyUser);
				trans.add(userRole, Transaction.INSERT);

			}
			CacheManager.set(PlatformCache.ProviderID, "UserRole", user.getUserName(), roleCodes);
		}

		if (trans.commit()) {
			// 是否使用当前用户信息直接登录
			Mapx<String, Object> map = user.toCaseIgnoreMapx();
			if (map.containsKey("password")) {
				map.remove("password");
			}
			map.put("userrolecode", PlatformUtil.getRoleCodesByUserName(user.getUserName()));
			map.put("userrolename", PlatformUtil.getRoleNames(PlatformUtil.getRoleCodesByUserName(user.getUserName())));
			response.setStatus(STATUS_SUCCESS);
			response.setMessage(Lang.get("Common.ExecuteSuccess"));
			response.setMapx(map);
		} else {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Common.ExecuteFailed"));
		}
	}
}
