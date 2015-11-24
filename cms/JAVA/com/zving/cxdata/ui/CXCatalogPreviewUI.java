package com.zving.cxdata.ui;

import com.zving.contentcore.ContentCorePlugin;
import com.zving.contentcore.template.TemplateContextUtil;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringFormat;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCDefaultTemplate;
import com.zving.schema.ZCPlatformProperty;
import com.zving.staticize.template.ITemplateType;
import com.zving.staticize.template.TemplateInstance;

public class CXCatalogPreviewUI
  extends UIFacade
{
  @Priv
  @Alias("cxcatalog/preview")
  public void preview(ZAction za)
  {
    String id = $V("ID");
    if ((ObjectUtil.empty(id)) || (!NumberUtil.isLong(id))) {
      return;
    }
    ZCCatalog catalog = CatalogUtil.getDAO(Long.parseLong(id));
    if (catalog == null) {
      return;
    }
    preview(catalog, za);
  }
  
  @Priv(login=false)
  @Alias("cxcatalog/show")
  public void interactive(ZAction za)
  {
    String id = $V("ID");
    if ((ObjectUtil.empty(id)) || (!NumberUtil.isLong(id))) {
      return;
    }
    ZCCatalog catalog = CatalogUtil.getDAO(Long.parseLong(id));
    if (catalog == null) {
      return;
    }
    interactive(catalog, za);
  }
  
  public void preview(ZCCatalog catalog, ZAction za)
  {
    generate(catalog, za, true);
  }
  
  public void interactive(ZCCatalog catalog, ZAction za)
  {
    generate(catalog, za, false);
  }
  
  private void generate(ZCCatalog catalog, ZAction za, boolean isPreview)
  {
    String platformID = za.getRequest().getParameter("platformID");
    if (StringUtil.isEmpty(platformID)) {
      platformID = "pc";
    }
    String indexTemplate = "";
    String listTemplate = "";
    if ("pc".equals(platformID))
    {
      indexTemplate = catalog.getIndexTemplate();
      listTemplate = catalog.getListTemplate();
    }
    else
    {
      ZCPlatformProperty t = PublishPlatformUtil.getPlatformProperty(catalog.getSiteID(), platformID, catalog.getID());
      if (t != null)
      {
        indexTemplate = t.getIndexTemplate();
        listTemplate = t.getListTemplate();
      }
    }
    if (StringUtil.isEmpty(listTemplate))
    {
      ZCDefaultTemplate dTemplate = SiteUtil.getDefaultTemplate(catalog.getSiteID(), platformID, catalog.getContentType());
      if (dTemplate != null) {
        listTemplate = dTemplate.getListTemplate();
      }
    }
    boolean list = YesOrNo.isYes(za.getRequest().getParameter("list"));
    String template = "";
    if (!list) {
      template = indexTemplate;
    }
    if (ObjectUtil.empty(template)) {
      template = listTemplate;
    }
    if (ObjectUtil.empty(template))
    {
      za.writeHTML(Lang.get("Contentcore.NoSetIndexOrListTemplate") + Lang.get("Contentcore.Catalog.Name") + ":" + catalog.getName());
      return;
    }
    long id = catalog.getID();
    template = PublishPlatformUtil.getPublishPlatformRoot(catalog.getSiteID(), platformID) + template;
    try
    {
      TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(template);
      ITemplateType tt = ContentCorePlugin.getStaticizeContext().getTemplateType(tpl.getConfig().getType());
      if (tt == null)
      {
        StringFormat sf = new StringFormat(Lang.get("Contentcore.TemplateTypeNotFound"), new Object[] { tpl.getConfig().getType() });
        za.writeHTML(sf.toString());
        return;
      }
      AbstractExecuteContext context = tt.getContext(id, platformID, isPreview);
    //设置请求参数到上下文
      context.addDataVariable("cxParam", this.Request);
      
      
      //context.setInteractive(isInteractive);
     // Current.getRequest().put("IsPreview", Boolean.valueOf(context.isPreview()));
      //context.addDataVariable("Request", Current.getRequest());
      context.addDataVariable("PlatformID", platformID);
     //TemplateContextUtil.addGlobalVariables(context);
      if (context.isPreview())
      {
        context.addDataVariable("FirstFileName", context.eval("FrontAppContext") + "catalog/preview?ID=" + id + 
          "&platformID=" + platformID);
        context.addDataVariable("OtherFileName", Config.getContextPath() + 
          "catalog/preview?PageIndex=${PageIndex}&ID=" + id + "&platformID=" + platformID);
      }
      else
      {
        context.addDataVariable("FirstFileName", Config.getContextPath() + "catalog/show?ID=" + id + "&platformID=" + 
          platformID);
        context.addDataVariable("OtherFileName", Config.getContextPath() + "catalog/show?PageIndex=${PageIndex}&ID=" + 
          id + "&platformID=" + platformID);
      }
      String suffix = PublishPlatformUtil.getSuffix(catalog.getSiteID(), platformID);
      if (".xml".equals(suffix)) {
        za.setContentType("text/xml");
      } else if (".json".equals(suffix)) {
        za.setContentType("text/plain");
      } else {
        za.setContentType("text/html");
      }
      tpl.setContext(context);
      tpl.setWriter(za.getResponse().getWriter());
      tpl.execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      za.writeHTML("<pre>" + StringUtil.htmlEncode(e.getMessage()) + "</pre>");
    }
  }
}


/* Location:           D:\work\workspace\zcms22426\UI\WEB-INF\plugins\lib\com.zving.contentcore.plugin.jar
 * Qualified Name:     com.zving.contentcore.ui.CatalogPreviewUI
 * JD-Core Version:    0.7.0.1
 */