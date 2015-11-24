package com.zving.platform.privilege;

/**
 * 系统日志菜单权限项<br>
 * 
 * @author 张金灿
 * @mail zjc@zving.com
 * @date 2014-7-18
 */
public class ApiPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.API";

	public ApiPriv() {
		super(MenuID, "@{Platform.Menu.NoPriv}");
	}
}
