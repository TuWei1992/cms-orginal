package com.zving.platform.privilege;

/**
 * 元数据管理菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class MetadataPriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Metadata";
	public static final String Add = MenuID + ".Add";
	public static final String Save = MenuID + ".Save";
	public static final String Delete = MenuID + ".Delete";
	public static final String AddData = MenuID + ".AddData";
	public static final String EditData = MenuID + ".EditData";
	public static final String DeleteData = MenuID + ".DeleteData";

	public MetadataPriv() {
		super(MenuID, null);
		addItem(Add, "@{Common.Add}");
		addItem(Save, "@{Common.Edit}");
		addItem(Delete, "@{Common.Delete}");
		addItem(AddData, "@{Platform.AddData}");
		addItem(EditData, "@{Platform.EditData}");
		addItem(DeleteData, "@{Platform.DeleteData}");
	}
}
