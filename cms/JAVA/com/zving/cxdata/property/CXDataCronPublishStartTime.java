package com.zving.cxdata.property;

import com.zving.contentcore.IProperty;
import com.zving.contentcore.property.AbstractProperty;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.ObjectUtil;

import java.util.Date;

public class CXDataCronPublishStartTime
  extends AbstractProperty
{
  public static final String ID = "CXDataCronPublishStartTime";
  
  public String getExtendItemID()
  {
    return ID;
  }
  
  public String getExtendItemName()
  {
    return "车享数据定时发布起始时间";
  }
  
  public boolean validate(String value)
  {
    return DateUtil.isDateTime(value);
  }
  
  public String defaultValue()
  {
    return DateUtil.getCurrentDateTime();
  }
  
  public static Date getValue(String props)
  {
    String v = PropertyUtil.getValue(props, ID);
    if (ObjectUtil.notEmpty(v)) {
      return DateUtil.parseDateTime(v);
    }
    return new Date(0L);
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
 * Qualified Name:     com.zving.contentcore.property.impl.CronPublishStartTime
 * JD-Core Version:    0.7.0.1
 */