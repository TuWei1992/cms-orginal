package com.zving.contentcore;

import java.util.Map;

import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.IExtendItem;

public abstract interface IProperty extends IExtendItem {
	public static final int Site = 1;
	public static final int Catalog = 2;
	public static final int Content = 4;
	@Deprecated
	public static final int SiteAndCatalogAndContent = 7;
	@Deprecated
	public static final int SiteAndCatalog = 3;
	@Deprecated
	public static final int SiteOnly = 1;
	@Deprecated
	public static final int CatalogOnly = 2;
	@Deprecated
	public static final int ContentOnly = 4;

	public abstract boolean validate(String paramString);

	public abstract String process(Mapx<String, Object> paramMapx);

	public abstract String defaultValue();

	public abstract String getContentType();

	public abstract int getUseType();

	public abstract boolean hasUseType(int paramInt);

	public boolean keepParam(String key);
	
	public void addSupplement(Mapx<String, String> configProps, Mapx<String, Object> request);
}
