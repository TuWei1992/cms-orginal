package com.zving.platform.privilege;

/**
 * 代码管理菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class CodePriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Code";
	public static final String Add = MenuID + ".Add";
	public static final String Edit = MenuID + ".Edit";
	public static final String Delete = MenuID + ".Delete";

	public CodePriv() {
		super(MenuID, null);
		addItem(Add, "@{Common.Add}");
		addItem(Edit, "@{Common.Edit}");
		addItem(Delete, "@{Common.Delete}");
	}
}
