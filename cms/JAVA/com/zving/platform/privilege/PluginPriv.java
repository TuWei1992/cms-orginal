package com.zving.platform.privilege;

/**
 * 插件管理菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-21
 */
public class PluginPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Plugin";
	public static final String Start = MenuID + ".Start";
	public static final String Stop = MenuID + ".Stop";

	public PluginPriv() {
		super(MenuID, null);
		addItem(Start, "@{Common.Start}");
		addItem(Stop, "@{Common.Stop}");
	}
}
