package com.zving.platform.privilege;

/**
 * 用户管理菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class UserPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.User";
	public static final String Add = MenuID + ".Add";
	public static final String Edit = MenuID + ".Edit";
	public static final String Delete = MenuID + ".Delete";
	public static final String SetPriv = MenuID + ".SetPriv";
	public static final String ChangePassword = MenuID + ".ChangePassword";
	public static final String Disable = MenuID + ".Disable";
	public static final String Enable = MenuID + ".Enable";

	public UserPriv() {
		super(MenuID, null);
		addItem(Add, "@{Common.Add}");
		addItem(Edit, "@{Common.Edit}");
		addItem(Delete, "@{Common.Delete}");
		addItem(SetPriv, "@{Platform.Role.SetPriv}");
		addItem(ChangePassword, "@{Common.ChangePassword}");
		addItem(Disable, "@{User.Disable}");
		addItem(Enable, "@{User.Enable}");
	}
}
