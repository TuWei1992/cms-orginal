package com.zving.contentadvance.tag;

import com.zving.contentadvance.util.ContentAdvanceCache;
import com.zving.contentcore.tag.function.ImageSize;
import com.zving.cxdata.bl.ResourceURLBL;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.schema.ZCPublishPoint;

public class AdvanceImageSize
  extends ImageSize
{
  protected String getRelativePath(AbstractExecuteContext context, String url)
  {
    DAOSet<ZCPublishPoint> set = ContentAdvanceCache.getPublishPoints(context.evalLong("Site.ID"));
    if ((set != null) && (url.contains(":/"))) {
      for (ZCPublishPoint p : set) {
        if (url.startsWith(p.getURLPrefix()))
        {
          url = url.substring(p.getURLPrefix().length());
          break;
        }
      }
    }
    url = ResourceURLBL.getImageRelativePath(url);
    return super.getRelativePath(context, url);
  }
}
