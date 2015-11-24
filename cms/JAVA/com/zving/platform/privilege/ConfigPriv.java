package com.zving.platform.privilege;

/**
 * 配置项管理菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class ConfigPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Config";
	public static final String Save = MenuID + ".Save";

	public ConfigPriv() {
		super(MenuID, null);
		addItem(Save, "@{Common.Save}");
	}
}
