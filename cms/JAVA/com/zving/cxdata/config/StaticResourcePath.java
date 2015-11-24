package com.zving.cxdata.config;

import java.io.File;

import com.zving.cxdata.CXDataPlugin;
import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;

public class StaticResourcePath
  extends FixedConfigItem
{
  public static final String ID = "StaticResourcePath";
  
  public StaticResourcePath()
  {
    super(ID, "ShortText", "Text", "静态资源文件路径", CXDataPlugin.ID);
  }
  
  public static String getValue()
  {
    String v = Config.getValue(ID);
    return v;
  }
  
}
