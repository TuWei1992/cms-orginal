package com.zving.cxdata.action;

import java.io.File;

import com.zving.contentcore.ContentCorePlugin;
import com.zving.contentcore.IContentType;
import com.zving.contentcore.IPublishPlatform;
import com.zving.contentcore.bl.PublishBL;
import com.zving.contentcore.bl.PublishLogBL;
import com.zving.contentcore.bl.PublishPlatformBL;
import com.zving.contentcore.point.AfterCatalogPublish;
import com.zving.contentcore.service.ContentTypeService;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.PublishPlatformUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.cxdata.CXDataContentType;
import com.zving.cxdata.ICXDataCondition;
import com.zving.cxdata.property.CXDataPublishConditionProp;
import com.zving.cxdata.service.CXDataConditionService;
import com.zving.framework.data.DataRow;
import com.zving.framework.data.DataTable;
import com.zving.framework.i18n.Lang;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.template.TemplateWriter;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.code.YesOrNo;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCDefaultTemplate;
import com.zving.schema.ZCPlatformProperty;
import com.zving.staticize.template.ITemplateType;
import com.zving.staticize.template.TemplateInstance;
/**
 * 栏目发布扩展动态，按条件发布页面，条件在栏目配置项里配置
 */
public class AfterCatalogPublishAction extends AfterCatalogPublish {

	@Override
	public boolean isUsable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void execute(ZCCatalog catalog, IPublishPlatform pt) {
		if (catalog.getContentType().equals(CXDataContentType.ID)) {
			String publishCondition = CXDataPublishConditionProp.getValue(catalog.getConfigProps());
			if (StringUtil.isNotEmpty(publishCondition)) {
				ICXDataCondition con = CXDataConditionService.getInstance().get(publishCondition);
				if (con != null) {
					String platformID = pt.getExtendItemID();
				    if (StringUtil.isEmpty(platformID)) {
				      platformID = "pc";
				    }
				    if (catalog.getVisibleFlag().equals("N")) {
				      return;
				    }
				    if (!YesOrNo.isYes(catalog.getGenerateFlag())) {
				      return;
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
				    boolean noIndexTemplate = ObjectUtil.empty(indexTemplate);
				    if (ObjectUtil.empty(listTemplate))
				    {
				      ZCDefaultTemplate t = SiteUtil.getDefaultTemplate(catalog.getSiteID(), platformID, catalog.getContentType());
				      if (t != null) {
				        listTemplate = t.getListTemplate();
				      }
				    }
				    if ((noIndexTemplate) && (ObjectUtil.empty(listTemplate))) {
				    	PublishBL.message(Lang.get("Contentcore.NoSetIndexOrListTemplate") + Lang.get("Contentcore.Catalog.Name") + ":" + catalog.getName(), 
				        platformID);
				    	return;
				    }
				    String siteRoot = PublishPlatformUtil.getPublishPlatformRoot(catalog.getSiteID(), platformID);
				    if ((StringUtil.isEmpty(listTemplate)) || (!FileUtil.exists(siteRoot + listTemplate))) {
				    	PublishBL.message(Lang.get("Contentcore.TemplateNotFount") + catalog.getName() + ":" + siteRoot + listTemplate, platformID);
				      return;
				    }
				    if ((!noIndexTemplate) && (!FileUtil.exists(siteRoot + indexTemplate))) {
				    	PublishBL.message(Lang.get("Contentcore.TemplateNotFount") + catalog.getName() + ":" + siteRoot + indexTemplate, platformID);
				    	return;
				    }
				    IContentType contentType = ContentTypeService.getContentType(catalog.getContentType(), catalog.getSiteID());
				    if (ObjectUtil.empty(contentType)) {
				    	PublishBL.message("Content type '" + contentType + "' has not registed!", platformID);
				    	return;
				    }
				    ITemplateType tt = ContentCorePlugin.getStaticizeContext().getTemplateType(contentType.getListTemplateTypeID());
			
				    String path = CatalogUtil.getFullPath(catalog.getID());
				    File f = new File(siteRoot + path);
				    if (!f.exists()) {
				      f.mkdirs();
				    }
				    AbstractExecuteContext context = tt.getContext(catalog.getID(), platformID,  false);
				    //23184-24115
				    //TemplateContextUtil.addPublishVariables(context);
				    
				    context = PublishPlatformBL.dealPlatformProperty(context, catalog.getID(), catalog.getSiteID(), platformID);
				    context.addDataVariable("PlatformID", platformID);
				    
				    String staticFileType = PublishPlatformBL.getStaticFileType(catalog.getSiteID(), platformID);
				    context.addDataVariable("FirstFileName", "list" + staticFileType);
				    context.addDataVariable("OtherFileName", 
				      "list_${PageIndex}" + staticFileType);
				    
				    DataTable args = con.getOptions();
				    for (DataRow arg : args) {
				    	context.addDataVariable(con.getArgName(), arg.getString(0));
					    String fileName = siteRoot + path + arg.getString(0)  + PublishPlatformUtil.getSuffix(catalog.getSiteID(), platformID);
					    try
					    {
					      if (!noIndexTemplate)
					      {
					        TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(siteRoot + indexTemplate);
					        tpl.setContext(context);
					        
					        TemplateWriter writer = new TemplateWriter();
					        tpl.setWriter(writer);
					        tpl.execute();
					        FileUtil.writeText(fileName, writer.getResult());
					        fileName = siteRoot + path + arg.getString(0) +"_list" + PublishPlatformUtil.getSuffix(catalog.getSiteID(), platformID);
					      }
					      TemplateInstance tpl = ContentCorePlugin.getStaticizeContext().getTemplateManager().find(siteRoot + listTemplate);
					      tpl.setContext(context);
					      
					      TemplateWriter writer = new TemplateWriter();
					      tpl.setWriter(writer);
					      tpl.execute();
					      FileUtil.writeText(fileName, writer.getResult());
					    }
					    catch (Exception e) {
					      e.printStackTrace();
					      PublishLogBL.addCatalogPublishLog(catalog, platformID, e.getMessage());
					      Errorx.addError(e.getMessage());  
					    }
				    }
				}
			}
		}
	}

}
