package com.zving.cxdata.template;

import com.zving.contentcore.template.types.AbstractCatalogListTemplate;
import com.zving.cxdata.CXDataPlugin;
/**
 * 
 * @author v_zhouquan
 * 车享数据栏目列表页模板
 */
public class CXDataListTemplate
  extends AbstractCatalogListTemplate
{
  public static final String ID = "CXDataList";
  
  public String getExtendItemID()
  {
    return ID;
  }
  
  public String getExtendItemName()
  {
    return "车享数据列表";
  }
  
  public String getPluginID()
  {
    return CXDataPlugin.ID;
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.media.plugin.jar
 * Qualified Name:     com.zving.media.template.AudioListTemplate
 * JD-Core Version:    0.7.0.1
 */