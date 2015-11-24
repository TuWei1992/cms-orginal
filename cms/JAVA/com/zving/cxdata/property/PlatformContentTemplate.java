package com.zving.cxdata.property;

import java.util.Map;

import com.zving.contentcore.IProperty;
import com.zving.contentcore.property.AbstractProperty;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.framework.collection.Mapx;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

public class PlatformContentTemplate extends AbstractProperty {
	public static final String ID = "platformContentTemplate";
	public static final String NAME = "发布平台内容模板";
	
	@Override
	public String defaultValue() {
		return null;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getUseType() {
		// TODO Auto-generated method stub
		return IProperty.Content;
	}

	@Override
	public boolean validate(String paramString) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getExtendItemID() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public String getExtendItemName() {
		// TODO Auto-generated method stub
		return NAME;
	}
	
	@Override
	public boolean keepParam(String key) {
		return key != null && key.startsWith(ID + "_");
	}
	
	@Override
	public void addSupplement(Mapx<String, String> configProps, Mapx<String, Object> request) {
		for (String key : request.keySet()) {
			if (keepParam(key)) {
				if (StringUtil.isNotEmpty(request.getString(key))) {
					configProps.put(key, request.getString(key));
				} else {
					configProps.remove(key);
				}
			}
		}
	}
	
	public static String getTemplate(String props, String platformID) {
		return  PropertyUtil.getValue(props, ID + "_" + platformID);
	}
	
}
