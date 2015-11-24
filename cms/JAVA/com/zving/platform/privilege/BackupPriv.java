package com.zving.platform.privilege;

/**
 * 数据备份菜单权限项<br>
 * 
 * @author 范梦媛
 * @mail fmy@zving.com
 * @date 2013-10-23
 */
public class BackupPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Backup";
	public static final String Backup = MenuID + ".Backup";
	public static final String Reset = MenuID + ".Reset";
	public static final String Delete = MenuID + ".Delete";
	public static final String Download = MenuID + ".Download";

	public BackupPriv() {
		super(MenuID, null);
		addItem(Backup, "@{Platform.DataBackup.Backup}");
		addItem(Reset, "@{Platform.DataBackup.Reset}");
		addItem(Delete, "@{Platform.DataBackup.DelFile}");
		addItem(Download, "@{Platform.DataBackup.Download}");
	}
}
