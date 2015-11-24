package com.zving.cxdata.property;

import com.zving.contentcore.IProperty;
import com.zving.contentcore.property.AbstractProperty;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;

public class CXDataCronPublishPeriod
  extends AbstractProperty
{
  public static final String ID = "CXDataCronPublishPeriod";
  
  public String getExtendItemID()
  {
    return ID;
  }
  
  public String getExtendItemName()
  {
    return "车享数据定时发布执行周期";
  }
  
  public boolean validate(String value)
  {
    return NumberUtil.isInt(value);
  }
  
  public String defaultValue()
  {
    return "24";
  }
  
  public static int getValue(String props)
  {
    String value = PropertyUtil.getValue(props, ID);
    if (ObjectUtil.empty(value)) {
      return 5;
    }
    return Integer.parseInt(value);
  }
  
  public String getContentType()
  {
    return null;
  }
  
  public int getUseType()
  {
    return  IProperty.Site + IProperty.Catalog;
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.contentcore.plugin.jar
 * Qualified Name:     com.zving.contentcore.property.impl.CronPublishPeriod
 * JD-Core Version:    0.7.0.1
 */