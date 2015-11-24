package com.zving.platform.privilege;

/**
 * 组织机构菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class BranchPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Branch";
	public static final String Add = MenuID + ".Add";
	public static final String Edit = MenuID + ".Edit";
	public static final String Delete = MenuID + ".Delete";
	public static final String SetPrivRange = MenuID + ".SetPrivRange";

	public BranchPriv() {
		super(MenuID, null);
		addItem(Add, "@{Common.Add}");
		addItem(Edit, "@{Common.Edit}");
		addItem(Delete, "@{Common.Delete}");
		addItem(SetPrivRange, "@{Platform.Branch.SetPrivRange}");
	}
}
