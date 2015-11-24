package com.zving.platform.api.method;

import java.util.Date;

import com.zving.framework.cache.CacheManager;
import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.i18n.LangUtil;
import com.zving.framework.security.LicenseInfo;
import com.zving.framework.security.PasswordUtil;
import com.zving.framework.security.Privilege;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.AbstractAPIMethod;
import com.zving.platform.api.APIRequest;
import com.zving.platform.api.APIResponse;
import com.zving.platform.bl.UserBL;
import com.zving.platform.code.YesOrNo;
import com.zving.platform.meta.MetaUtil;
import com.zving.platform.meta.SystemModelType.SystemMetaModel;
import com.zving.platform.util.PlatformCache;
import com.zving.platform.util.PlatformUtil;
import com.zving.schema.ZDPrivilege;
import com.zving.schema.ZDUser;
import com.zving.schema.ZDUserRole;

public class UserAddAPIMethod extends AbstractAPIMethod {
	public static final String ID = "adduser";

	public UserAddAPIMethod() {
		addOutput("", "Mapx", DataTypes.OBJECT, false, "@{Platform.API.KeyValue}");
		addOutput("Mapx", "userrolecode", DataTypes.STRING, true, "@{Platform.API.RoleCode}");
		addOutput("Mapx", "userrolename", DataTypes.STRING, true, "@{Platform.API.RoleName}");
		addOutput("Mapx", "islogin", DataTypes.STRING, true, "@{Platform.API.IsLogin}");

		addParam("adduser", "@{Common.AddUser}", DataTypes.STRING, false);
		addParam("username", "@{Common.UserName}", DataTypes.STRING, false);
		addParam("userpassword", "@{Common.Password}", DataTypes.STRING, false);
		addParam("confirmpassword", "@{User.ConfirmPassword}", DataTypes.STRING, false);
		addParam("branchinnercode", "@{Platform.BranchInnercode}", DataTypes.STRING, false);
		addParam("email", "@{User.Email}", DataTypes.STRING, false);
		addParam("rolecode", "@{Platform.RoleCode}", DataTypes.STRING, true);
	}

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.API.AddUser}";
	}

	@Override
	public void execute(APIRequest request, APIResponse response) {
		Mapx<String, Object> params = request.getParameters();
		if (new Q("select count(*) from ZDUser").executeInt() >= LicenseInfo.getUserLimit()) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.LicenseWarning"));
			return;
		}

		Transaction trans = new Transaction();
		String addUser = params.getString("adduser");
		if (StringUtil.isEmpty(addUser)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：adduser");
			return;
		}
		String userName = params.getString("username");
		if (StringUtil.isEmpty(userName)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：username");
			return;
		}
		String password = params.getString("userpassword");
		if (StringUtil.isEmpty(password)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：userpassword");
			return;
		}
		String cpassword = params.getString("confirmpassword");
		if (StringUtil.isEmpty(cpassword)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：confirmpassword");
			return;
		}
		if (!password.equals(cpassword)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Common.RetypePasswordError"));
			return;
		}
		String branchInnerCode = params.getString("branchinnercode");
		if (StringUtil.isEmpty(branchInnerCode)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：branchinnercode");
			return;
		}
		String email = params.getString("email");
		if (StringUtil.isEmpty(email)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.Provider.AddUser.ErrorMsg") + "：email");
			return;
		}

		if (!UserBL.UserPattern.matcher(userName).matches()) {
			response.setStatus(STATUS_FAILED);
			response.setMessage("username errormsg：" + Lang.get("User.UserNameVerify"));
			return;
		}
		ZDUser user = new ZDUser();
		user.setValue(params);
		user.setUserName(user.getUserName().toLowerCase());
		if (user.fill()) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(userName + Lang.get("Common.Exists"));
			return;
		}

		user.setPassword(PasswordUtil.generate(password));
		user.setAddTime(new Date());
		user.setAddUser(addUser);
		user.setRealName(LangUtil.getI18nFieldValue("RealName"));
		if (StringUtil.isNull(user.getStatus())) {
			user.setStatus(YesOrNo.Yes); // 新建用户默认为启用状态
		}
		if (StringUtil.isNull(user.getIsBranchAdmin())) {
			user.setIsBranchAdmin(YesOrNo.No);
		}
		MetaUtil.saveExtendData(params, user.getUserName(), SystemMetaModel.getValue(SystemMetaModel.User));
		trans.add(user, Transaction.INSERT);

		ZDPrivilege priv = new ZDPrivilege();
		priv.setOwnerType(Privilege.OwnerType_User);
		priv.setOwner(user.getUserName());
		priv.setAddTime(user.getAddTime());
		priv.setAddUser(user.getAddUser());
		trans.add(priv, Transaction.INSERT);

		// 角色
		String roleCodes = params.getString("rolecode");
		if (StringUtil.isNotEmpty(roleCodes)) {
			String[] RoleCodes = roleCodes.split(",");

			CacheManager.set(PlatformCache.ProviderID, "User", user.getUserName(), user);
			CacheManager.set(PlatformCache.ProviderID, "UserRole", user.getUserName(), roleCodes);

			for (String roleCode : RoleCodes) {
				if (StringUtil.isEmpty(roleCode) || StringUtil.isEmpty(user.getUserName())) {
					continue;
				}
				ZDUserRole userRole = new ZDUserRole();
				userRole.setUserName(user.getUserName());
				userRole.setRoleCode(roleCode);
				userRole.setAddTime(new Date());
				userRole.setAddUser(addUser);
				trans.add(userRole, Transaction.INSERT);
			}
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
			response.setMessage(Lang.get("Common.AddSuccess"));
			response.setMapx(map);
		} else {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Common.AddFailed"));
		}
		return;
	}
}
