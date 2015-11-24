package com.zving.cxdata.property;

import com.zving.contentcore.IProperty;
import com.zving.contentcore.property.AbstractProperty;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.framework.utility.StringUtil;

public class PlatformContentTemplateFlag extends AbstractProperty {
	public static final String ID = "PlatformContentTemplateFlag";
	public static final String NAME = "发布平台内容是否使用独立模板";
	
	@Override
	public String defaultValue() {
		return "NO";
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
	
	public static boolean templateFlag(String props, String platformID) {
		String flag = PropertyUtil.getValue(props, ID);
		if (StringUtil.isNotEmpty(flag) && flag.indexOf(platformID) != -1) {
			return true;
		}
		return false;
	}
}
