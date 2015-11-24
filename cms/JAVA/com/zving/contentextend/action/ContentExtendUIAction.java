package com.zving.contentextend.action;

import com.zving.contentcore.property.PropertyService;
import com.zving.contentcore.property.PropertyUtil;
import com.zving.contentcore.service.ContentTypeService;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.framework.Current;
import com.zving.framework.ResponseData;
import com.zving.framework.collection.Mapx;
import com.zving.framework.extend.ExtendException;
import com.zving.framework.extend.action.ZhtmlContext;
import com.zving.framework.extend.action.ZhtmlExtendAction;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.meta.MetaModel;
import com.zving.platform.meta.ModelTemplateService;
import com.zving.platform.util.PlatformCache;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZDMetaModel;
import com.zving.schema.ZDModelTemplate;

public class ContentExtendUIAction
  extends ZhtmlExtendAction
{
  public void execute(ZhtmlContext context)
    throws ExtendException
  {
    long catalogID = Current.getResponse().getLong("CatalogID");
    if (catalogID == 0L) {
      return;
    }
    MetaModel mm = null;
    ZCCatalog catalog = CatalogUtil.getDAO(catalogID);
    if (ContentTypeService.isRegistedContentType(catalog.getContentType()))
    {
      Mapx<String, String> map = PropertyUtil.parse(catalog.getConfigProps());
      PropertyService.getInstance().addCatalogDefaultValues(map, catalog.getContentType());
      String modelCode = map.getString("ContentModelCode");
      if (ObjectUtil.empty(modelCode)) {
        return;
      }
      mm = MetaModel.load(modelCode);
    }
    else
    {
      mm = PlatformCache.getMetaModel(catalog.getContentType());
    }
    if (mm == null)
    {
      LogUtil.warn("内容属性扩展模型不存在");
      return;
    }
    ZDModelTemplate mt = mm.getTemplateByType("Content");
    String tempate = (mt == null) || (StringUtil.isEmpty(mt.getTemplateContent())) ? ModelTemplateService.DefaultTemplate: mt
      .getTemplateContent();
    context.write(ModelTemplateService.parseModelTemplate(mm.getDAO().getID(), tempate));
  }
  
  public boolean isUsable()
  {
    return true;
  }
}
