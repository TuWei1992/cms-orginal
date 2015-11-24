package com.zving.platform.api.method;

import com.zving.framework.collection.Mapx;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.Q;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.AbstractAPIMethod;
import com.zving.platform.api.APIRequest;
import com.zving.platform.api.APIResponse;
import com.zving.platform.bl.PrivBL;
import com.zving.platform.util.PlatformUtil;

public class UserInfoGetAPIMethod extends AbstractAPIMethod {
	public static final String ID = "userinfo";

	public UserInfoGetAPIMethod() {
		addOutput("", "DataTable", DataTypes.OBJECT, false, "@{Platform.API.DataTable}");
		addOutput("DataTable", "UserName", DataTypes.STRING, false, "@{SysInfo.DBUser}");
		addOutput("DataTable", "RealName", DataTypes.STRING, true, "@{Platform.API.DataTable}");
		addOutput("DataTable", "Password", DataTypes.STRING, false, "@{Platform.API.UserPassword}");
		addOutput("DataTable", "BranchInnerCode", DataTypes.STRING, false, "@{Platform.API.BranchInnerCode}");
		addOutput("DataTable", "IsBranchAdmin", DataTypes.STRING, false, "@{Platform.API.IsBranchAdmin}");
		addOutput("DataTable", "Status", DataTypes.STRING, false, "@{Platform.API.UserStatus}");
		addOutput("DataTable", "Type", DataTypes.STRING, true, "@{Platform.API.UserType}");
		addOutput("DataTable", "Email", DataTypes.STRING, false, "@{Platform.API.Email}");
		addOutput("DataTable", "Tel", DataTypes.STRING, true, "@{Platform.API.Telephone}");
		addOutput("DataTable", "Mobile", DataTypes.STRING, true, "@{Platform.API.Mobile}");
		addOutput("DataTable", "LastLoginTime", DataTypes.DATETIME, true, "@{Platform.API.LastLoginTime}");
		addOutput("DataTable", "LastLoginIP", DataTypes.STRING, true, "@{Platform.API.LastLoginIP}");
		addOutput("DataTable", "Prop1", DataTypes.STRING, true, "@{Platform.API.Prop}");
		addOutput("DataTable", "Prop2", DataTypes.STRING, true, "@{Platform.API.Prop}");
		addOutput("DataTable", "Prop6", DataTypes.STRING, true, "@{Platform.API.Prop}");
		addOutput("DataTable", "Prop5", DataTypes.STRING, true, "@{Platform.API.Prop}");
		addOutput("DataTable", "Prop4", DataTypes.STRING, true, "@{Platform.API.Prop}");
		addOutput("DataTable", "Prop3", DataTypes.STRING, true, "@{Platform.API.Prop}");
		addOutput("DataTable", "Memo", DataTypes.STRING, true, "@{Platform.API.Memo}");
		addOutput("DataTable", "AddTime", DataTypes.DATETIME, false, "@{Common.AddTime}");
		addOutput("DataTable", "AddUser", DataTypes.STRING, false, "@{Common.AddUser}");
		addOutput("DataTable", "ModifyTime", DataTypes.DATETIME, true, "@{Platform.DataBackup.LastModifyTime}");
		addOutput("DataTable", "ModifyUser", DataTypes.STRING, true, "@{Platform.ModifyUser}");

		addParam("username", "@{Common.UserName}", DataTypes.STRING, false);
		addParam("columns", "@{Platform.FieldList}", DataTypes.STRING, true);
	}

	@Override
	public String getExtendItemID() {
		return ID;
	}

	@Override
	public String getExtendItemName() {
		return "@{Platform.Provider.UserInfo}";
	}

	@Override
	public void execute(APIRequest request, APIResponse response) {
		Mapx<String, Object> params = request.getParameters();
		String username = params.getString("username");
		Q qb = new Q("select * from ZDUser where 1=1");
		if (StringUtil.isNotEmpty(username)) {
			qb.append(" and UserName=?", username);
		}
		String columns = "," + params.getString("columns") + ",";
		DataTable dt = qb.executeDataTable();
		if (dt != null && dt.getRowCount() > 0) {
			dt.deleteColumn("Password");
			for (DataRow dr : dt) {
				if (columns.toLowerCase().indexOf("userpriv") > -1) {
					if (!dt.containsColumn("UserPriv")) {
						dt.insertColumn("UserPriv");
					}
					dr.set("UserPriv", PrivBL.getUserPriv(dr.getString("UserName")).toString());
				}
				if (columns.toLowerCase().indexOf("userrolecode") > -1) {
					if (!dt.containsColumn("UserRoleCode")) {
						dt.insertColumn("UserRoleCode");
					}
					dr.set("UserRoleCode", PlatformUtil.getRoleCodesByUserName(dr.getString("UserName")));
				}
				if (columns.toLowerCase().indexOf("userrolename") > -1) {
					if (!dt.containsColumn("UserRoleName")) {
						dt.insertColumn("UserRoleName");
					}
					dr.set("UserRoleName", PlatformUtil.getRoleNames(PlatformUtil.getRoleCodesByUserName(dr.getString("UserName"))));
				}
			}
		}
		response.setDataTable(params.getString("columns"), dt);
		response.setStatus(STATUS_SUCCESS);
	}
}
