package com.zving.cxdata.config;

import java.io.File;

import com.zving.cxdata.CXDataPlugin;
import com.zving.framework.Config;
import com.zving.framework.utility.ObjectUtil;
import com.zving.platform.FixedConfigItem;

public class LicensePath
  extends FixedConfigItem
{
  public static final String ID = "LicensePath";
  
  public LicensePath()
  {
    super(ID, "ShortText", "Text", "授权文件", CXDataPlugin.ID);
  }
  
  public static String getValue()
  {
    String v = Config.getValue(ID);
    if (ObjectUtil.empty(v)) {
      v = Config.getContextRealPath() + "WEB-INF/plugins/classes/license.dat";
    }
    return FixedConfigItem.replacePathHolder(v);
  }
  
  public static String getReadValue()
  {
    String v = Config.getValue(ID);
    if (ObjectUtil.notEmpty(v)) {
    	v = FixedConfigItem.replacePathHolder(v);
    	if (new File(v).exists()) {
    		return v;
    	}
    } 
	return null;
  }
  
  
}
