package com.zving.platform.privilege;

import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.IExtendItem;
import com.zving.framework.extend.menu.MenuManager;

/**
 * 菜单权限
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-18
 */
public class AbstractMenuPriv implements IExtendItem {
	private String MenuID;
	private Mapx<String, String> PrivItems = new Mapx<String, String>();
	private String Memo;

	public AbstractMenuPriv(String MenuID, String Memo) {
		this.Memo = Memo;
		this.MenuID = MenuID;
	}

	@Override
	public String getExtendItemID() {
		return MenuID;
	}

	public void addItem(String itemID, String name) {
		PrivItems.put(itemID, name);
	}

	public Mapx<String, String> getPrivItems() {
		return PrivItems;
	}

	@Override
	public String getExtendItemName() {
		return MenuManager.getMenu(MenuID).getName();
	}

	public String getMemo() {
		return Memo;
	}
}
