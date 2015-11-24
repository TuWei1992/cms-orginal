package com.zving.cxdata.config;

import java.io.File;

import com.zving.cxdata.CXDataPlugin;
import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;

public class ExcludeResourcePath
  extends FixedConfigItem
{
  public static final String ID = "ExcludeResourcePath";
  
  public ExcludeResourcePath()
  {
    super(ID, "ShortText", "Text", "排除资源文件路径", CXDataPlugin.ID);
  }
  
  public static String getValue()
  {
    String v = Config.getValue(ID);
    return v;
  }
  
}
