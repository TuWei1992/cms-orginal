package com.zving.platform;

import java.util.ArrayList;
import java.util.List;

import com.zving.framework.extend.IExtendItem;

/**
 * 系统中不可删除、某些代码项不可删除、要求支持多级代码的代码类别。
 * 
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-17
 */
public class FixedCodeType implements IExtendItem {
	boolean allowAddItem = true;
	boolean multiLevel = false;
	String codeType;
	String codeName;
	List<FixedCodeItem> fixedItems = new ArrayList<FixedCodeItem>();

	public FixedCodeType(String codeType, String codeName, boolean allowAddItem, boolean multiLevel) {
		this.codeType = codeType;
		this.codeName = codeName;
		this.allowAddItem = allowAddItem;
		this.multiLevel = multiLevel;
	}

	public boolean contains(String codeValue) {
		for (FixedCodeItem item : fixedItems) {
			if (item.getValue().equals(codeValue)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getExtendItemID() {
		return getCodeType();
	}

	public String getCodeType() {
		return codeType;
	}

	@Override
	public String getExtendItemName() {
		return getCodeName();
	}

	public List<FixedCodeItem> getFixedItems() {
		return fixedItems;
	}

	public void addFixedItem(String itemValue, String itemName, String icon) {
		addFixedItem(itemValue, itemName, icon, null);
	}

	public void addFixedItem(String itemValue, String itemName, String icon, String memo) {
		fixedItems.add(new FixedCodeItem(itemValue, itemName, icon, memo));
	}

	public boolean allowAddItem() {
		return allowAddItem;
	}

	public boolean isMultiLevel() {
		return multiLevel;
	}

	public void setCodeName(String codeName) {

		this.codeName = codeName;
	}

	public String getCodeName() {
		return codeName;
	}

	public static class FixedCodeItem {
		private String value;// CodeValue
		private String name;// CodeName
		private String icon;
		private String memo;

		public FixedCodeItem(String value, String name, String icon, String memo) {
			this.value = value;
			this.name = name;
			this.icon = icon;
			this.memo = memo;
		}

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getMemo() {
			return memo;
		}

		public void setMemo(String memo) {
			this.memo = memo;
		}

	}
}
