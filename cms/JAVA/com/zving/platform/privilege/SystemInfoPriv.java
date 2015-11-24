package com.zving.platform.privilege;

/**
 * 系统信息菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class SystemInfoPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.SystemInfo";
	public static final String ChangeLoginStatus = MenuID + ".ChangeLoginStatus";
	public static final String ForceExit = MenuID + ".ForceExit";
	public static final String Export = MenuID + ".Export";
	public static final String Import = MenuID + ".Import";
	public static final String Update = MenuID + ".Update";

	public SystemInfoPriv() {
		super(MenuID, null);
		addItem(ChangeLoginStatus, "@{SysInfo.AllowLogin}");
		addItem(ForceExit, "@{SysInfo.LogoutAll}");
		addItem(Export, "@{SysInfo.ExportDB}");
		addItem(Import, "@{SysInfo.ImportDB}");
		addItem(Update, "@{SystemInfo.Update}");
	}
}
