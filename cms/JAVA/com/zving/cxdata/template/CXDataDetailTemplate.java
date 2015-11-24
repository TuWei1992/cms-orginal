package com.zving.cxdata.template;

import com.zving.contentcore.template.types.AbstractDetailTemplate;
import com.zving.cxdata.CXDataPlugin;
import com.zving.framework.collection.Mapx;
/**
 * 
 * @author v_zhouquan
 * 车享数据栏目详情页模板s
 */
public class CXDataDetailTemplate
  extends AbstractDetailTemplate
{
  public static final String ID = "CXDataDetail";
  
  public String getExtendItemID()
  {
    return ID;
  }
  
  public String getExtendItemName()
  {
    return "车享数据详情";
  }
  
  public String getPluginID()
  {
    return CXDataPlugin.ID;
  }
  
  public String getVarPrefix(Mapx<String, Object> map)
  {
    return "CXData";
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.media.plugin.jar
 * Qualified Name:     com.zving.media.template.AudioDetailTemplate
 * JD-Core Version:    0.7.0.1
 */