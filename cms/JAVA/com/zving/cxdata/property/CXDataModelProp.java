package com.zving.cxdata.property;

import com.zving.contentcore.IProperty;
import com.zving.contentcore.property.AbstractProperty;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.cxdata.CXDataContentType;
/**
 * 
 * @author v_zhouquan
 * 栏目属性扩展项，选择数据获取模型
 */
public class CXDataModelProp extends AbstractProperty {
	public static final String ID = "CXDataModelProp";
	public static final String NAME = "车享数据模型";
	@Override
	public String defaultValue() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getUseType() {
		// TODO Auto-generated method stub
		return IProperty.Catalog;
	}

	@Override
	public boolean validate(String arg0) {
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
	
	public static String getValue(long  catalogid) {
		return CatalogUtil.getProperty(catalogid, ID, false);
	}
	
	public static String getValue(String  property) {
		return PropertyUtil.getValue(property, ID);
	}

}
