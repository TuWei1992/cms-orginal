package com.zving.platform.privilege;

/**
 * 回收站菜单权限项
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-18
 */
public class RecycleBinPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.RecycleBin";
	public static final String DeleteReally = MenuID + ".DeleteReally";
	public static final String Recovery = MenuID + ".Recovery";

	public RecycleBinPriv() {
		super(MenuID, null);
		addItem(DeleteReally, "@{Platform.CompletelyDelete}");
		addItem(Recovery, "@{Platform.DataBackup.Reset}");

	}
}
