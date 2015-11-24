package com.zving.cxdata.ui;

import java.util.Map;

import com.zving.contentcore.ContentCorePlugin;
import com.zving.contentcore.template.TemplateContextUtil;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.i18n.Lang;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringFormat;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCSite;
import com.zving.staticize.template.ITemplateType;
import com.zving.staticize.template.TemplateInstance;

public class CXSitePreviewUI
  extends UIFacade
{
  @Priv
  @Alias("site/preview")
  public void preview(ZAction za)
  {
    generate(za, true);
  }
  
  @Priv(login=false)
  @Alias("site/show")
  public void interactive(ZAction za)
  {
    generate(za, false);
  }
  
  private void generate(ZAction za, boolean isPreview) {
    long id = $L("ID");
    if (id == 0L) {
      return;
    }
    ZCSite site = SiteUtil.getDAO(id);
    if (site == null) {
      return;
    }
    
    String platformID = za.getRequest().getParameter("platformID");
    if (StringUtil.isEmpty(platformID)) {
      platformID = "pc";
    }
    String indexTemplate = PublishPlatformUtil.getSiteIndexTemplate(site.getID(), platformID);
    if (ObjectUtil.empty(indexTemplate))
    {
      za.writeHTML(Lang.get("Contentcore.NoSetSiteIndexTemplate"));
      return;
    }
    String siteRoot = PublishPlatformUtil.getPublishPlatformRoot(site.getID(), platformID);
    indexTemplate = siteRoot + indexTemplate;
    try
    {
      TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(indexTemplate);
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
      context.addDataVariable("PlatformID", platformID);
      
      //TemplateContextUtil.addGlobalVariables(context);
      //this.Request.put("IsPreview", Boolean.valueOf(context.isPreview()));
     // context.addDataVariable("Request", this.Request);
      
      String suffix = PublishPlatformUtil.getSuffix(site.getID(), platformID);
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

 * Qualified Name:     com.zving.contentcore.ui.SitePreviewUI

 * JD-Core Version:    0.7.0.1

 */