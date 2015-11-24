package com.zving.platform.meta;

import java.util.List;

import com.zving.framework.extend.IExtendItem;

/**
 * 元数据模型类型
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-2-15
 */
public interface IMetaModelType extends IExtendItem {
	@Override
	public String getExtendItemID();

	@Override
	public String getExtendItemName();
	/**
	 * 是否是系统扩展模型
	 * @return
	 */
	public boolean isSystemModel();
	/**
	 * 获得扩展模型模板类型
	 * @return
	 */
	public List<IMetaModelTemplateType> getTemplateTypes();
	/**
	 * 获得扩展模型默认模板
	 * @return
	 */
	public String getDefautlTemplateHtml();
}
