package com.zving.platform.privilege;

/**
 * 计划任务菜单权限项
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class SchedulePriv extends AbstractMenuPriv {
	public static final String MenuID = "Platform.Schedule";
	public static final String Add = MenuID + ".Add";
	public static final String Edit = MenuID + ".Edit";
	public static final String Delete = MenuID + ".Delete";
	public static final String ManualExecute = MenuID + ".ManualExecute";

	public SchedulePriv() {
		super(MenuID, null);
		addItem(Add, "@{Common.Add}");
		addItem(Edit, "@{Common.Edit}");
		addItem(Delete, "@{Common.Delete}");
		addItem(ManualExecute, "@{Platform.Schedule.ManualExecute}");
	}
}
