package com.zving.platform.api.method;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.i18n.Lang;
import com.zving.framework.security.PasswordUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.AbstractAPIMethod;
import com.zving.platform.api.APIRequest;
import com.zving.platform.api.APIResponse;
import com.zving.schema.ZDUser;

public class UserChangePasswordAPIMethod extends AbstractAPIMethod {
	public static final String ID = "changepassword";

	public UserChangePasswordAPIMethod() {

		addParam("username", "@{Common.UserName}", DataTypes.STRING, false);
		addParam("newpassword", "@{Common.NewPassword}", DataTypes.STRING, false);
	}

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.Provider.ChangePassword}";
	}

	@Override
	public void execute(APIRequest request, APIResponse response) {
		Mapx<String, Object> params = request.getParameters();
		String username = params.getString("username");
		if (StringUtil.isEmpty(username)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Contentcore.Catalog.LoseRequiredParams") + "：username");
			return;
		}
		ZDUser user = new ZDUser();
		user.setUserName(username);
		if (!user.fill()) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Platform.HasNoUser"));
			return;
		}

		String newpassword = params.getString("newpassword");
		if (StringUtil.isEmpty(newpassword)) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Contentcore.Catalog.LoseRequiredParams") + "：newpassword");
			return;
		}
		Q qb = new Q("update ZDUser set Password=? where UserName=?");
		qb.add(PasswordUtil.generate(newpassword));
		qb.add(username);
		if (qb.executeNoQuery() <= 0) {
			response.setStatus(STATUS_FAILED);
			response.setMessage(Lang.get("Common.ExecuteFailed"));
			return;
		}
		response.setStatus(STATUS_SUCCESS);
		response.setMessage(Lang.get("Common.ExecuteSuccess"));
	}
}
