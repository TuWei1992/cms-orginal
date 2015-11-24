package com.zving.cxdata.property;

import com.zving.contentcore.IProperty;
import com.zving.contentcore.property.AbstractProperty;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.framework.utility.ObjectUtil;

public class CXDataCronPublishPeriodType
  extends AbstractProperty
{
  public static final String ID = "CXDataCronPublishPeriodType";
  
  public String getExtendItemID()
  {
    return ID;
  }
  
  public String getExtendItemName()
  {
    return "车享数据定时发布执行周期单位";
  }
  
  public boolean validate(String value)
  {
    return true;
  }
  
  public String defaultValue()
  {
    return "Hour";
  }
  
  public static String getValue(String props)
  {
    String value = PropertyUtil.getValue(props, ID);
    if (ObjectUtil.empty(value)) {
      return new CXDataCronPublishPeriodType().defaultValue();
    }
    return value;
  }
  
  public String getContentType()
  {
    return null;
  }
  
  public int getUseType()
  {
    return IProperty.Site + IProperty.Catalog;
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.contentcore.plugin.jar
 * Qualified Name:     com.zving.contentcore.property.impl.CronPublishPeriodType
 * JD-Core Version:    0.7.0.1
 */