package com.zving.platform.privilege;

/**
 * 角色管理菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class RolePriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Role";
	public static final String Add = MenuID + ".Add";
	public static final String Edit = MenuID + ".Edit";
	public static final String Delete = MenuID + ".Delete";
	public static final String SetPriv = MenuID + ".SetPriv";
	public static final String AddUser = MenuID + ".AddUser";
	public static final String RemoveUser = MenuID + ".RemoveUser";

	public RolePriv() {
		super(MenuID, null);
		addItem(Add, "@{Common.Add}");
		addItem(Edit, "@{Common.Edit}");
		addItem(Delete, "@{Common.Delete}");
		addItem(SetPriv, "@{Platform.Role.SetPriv}");
		addItem(AddUser, "@{Role.AddUser}");
		addItem(RemoveUser, "@{Role.RemoveUser}");
	}
}
