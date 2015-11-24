package com.zving.cxdata.property;

import com.zving.contentcore.IProperty;
import com.zving.contentcore.property.AbstractProperty;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;

public class CronPublishDetailEnable
  extends AbstractProperty
{
  public static final String ID = "CronPublishDetailEnable";
  
  public String getExtendItemID()
  {
    return ID;
  }
  
  public boolean validate(String value)
  {
    return (YesOrNo.isNo(value)) || (YesOrNo.isYes(value));
  }
  
  public String defaultValue()
  {
    return "N";
  }
  
  public String getExtendItemName()
  {
    return "栏目定时发布是否生成详细页";
  }
  
  public static boolean getValue(String props)
  {
    String value = PropertyUtil.getValue(props, ID);
    if (StringUtil.isNotNull(value)) {
      return YesOrNo.isYes(value);
    }
    return YesOrNo.isYes(new CronPublishDetailEnable().defaultValue());
  }
  
  public String getContentType()
  {
    return null;
  }
  
  public int getUseType()
  {
     return IProperty.Catalog;
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.contentcore.plugin.jar
 * Qualified Name:     com.zving.contentcore.property.impl.CronPublishEnable
 * JD-Core Version:    0.7.0.1
 */