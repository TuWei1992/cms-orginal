package com.zving.platform.privilege;

/**
 * 系统日志菜单权限项<br>
 * 
 * @author 范梦媛
 * @mail fmy@zving.com
 * @date 2013-10-23
 */
public class LogPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Log";

	public LogPriv() {
		super(MenuID, "@{Platform.Menu.NoPriv}");
	}
}
