package com.zving.contentcore.property;

import com.zving.contentcore.IProperty;
import com.zving.framework.collection.Mapx;

public abstract class AbstractProperty implements IProperty {
	public String process(Mapx<String, Object> map) {
		return map.getString(getExtendItemID());
	}

	public boolean hasUseType(int useType) {
		return (getUseType() & useType) == useType;
	}

	public boolean keepParam(String key) {
		return getExtendItemID().equals(key);
	}
	
	@Override
	public void addSupplement(Mapx<String, String> configProps, Mapx<String, Object> request) {
		
	}
}
