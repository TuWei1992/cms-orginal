package com.zving.cxdata.ui;



import java.util.Iterator;

import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.service.PublishPlatformService;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.data.DataTable;
import com.zving.framework.utility.StringUtil;

@Alias("PlatformPreview")
public class CXPreviewUI
  extends UIFacade
{
  @Priv
  public void init()
  {
    DataTable dt = PublishPlatformService.getUsedPublishPlatformDataTable(SiteBL.getCurrentSite());
    if ((dt != null) && (dt.getRowCount() > 0)) {
      $S("defaultPlatform", dt.get(0, "ID"));
    } else {
      $S("defaultPlatform", "pc");
    }
    $S("siteID", Long.valueOf(SiteBL.getCurrentSite()));
    
    String path = $V("path");
    String id = $V("ID");
    String conentType = $V("ContentType");
    String url = path + "?ID=" + id;
    for (Iterator<String> iterator = this.Request.keySet().iterator();iterator.hasNext();) {
    	String key = iterator.next();
    	if (!key.equals("ID")) {
    		url += "&" + key+"=" + $V(key);
    	}
    }

    $S("previewURL", url);
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.contentcore.plugin.jar
 * Qualified Name:     com.zving.contentcore.ui.PreviewUI
 * JD-Core Version:    0.7.0.1
 */