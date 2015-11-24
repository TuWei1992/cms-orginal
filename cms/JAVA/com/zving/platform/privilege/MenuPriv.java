package com.zving.platform.privilege;

/**
 * &quot;菜单管理&quot;菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class MenuPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Menu";
	public static final String Start = MenuID + ".Start";
	public static final String Stop = MenuID + ".Stop";
	/*public static final String Add = MenuID + ".Add";
	public static final String Edit = MenuID + ".Edit";
	public static final String Del = MenuID + ".Delete";*/
	public static final String Reset = MenuID + ".Reset";
	public static final String Sort = MenuID + ".Sort";

	public MenuPriv() {
		super(MenuID, null);
		addItem(Start, "@{Platform.Enabled}");
		addItem(Stop, "@{Platform.Disabled}");
		/*addItem(Add, "@{Menu.NewMenu}");
		addItem(Edit, "@{Platform.Menu.Edit}");
		addItem(Del, "@{Common.Delete}");*/
		addItem(Reset, "@{Platform.Menu.Reset}");
		addItem(Sort, "@{Platform.Menu.Sort}");
	}
}
