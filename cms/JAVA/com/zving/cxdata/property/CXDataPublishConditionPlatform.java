package com.zving.cxdata.property;

import com.zving.contentcore.IProperty;
import com.zving.contentcore.property.AbstractProperty;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCSite;

/**
 * 
 * @author v_zhouquan
 * 栏目属性扩展项，选择数据按条件发布
 */
public class CXDataPublishConditionPlatform extends AbstractProperty {
	public static final String ID = "CXDataPublishConditionPlatform";
	public static final String NAME = "使用发布条件的平台";
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
		return IProperty.Catalog + IProperty.Site;
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
	public static String getValue(ZCCatalog catalog) {
		return CatalogUtil.getProperty(catalog.getID(), ID, false);
	}
	
	public static String getValue(ZCSite site) {
		return SiteUtil.getPropertyValue(site.getID(), ID);
	}
	
	public static String getValue(String  property) {
		return PropertyUtil.getValue(property, ID);
	}
	
}
