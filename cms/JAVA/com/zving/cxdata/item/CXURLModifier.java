package com.zving.cxdata.item;

import com.zving.contentadvance.bl.PublishPointBL;
import com.zving.contentcore.ICoreURLModifier;
import com.zving.contentcore.code.DetailNameRule;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.contentcore.util.SiteUtil;
import com.zving.cxdata.bl.ResourceURLBL;
import com.zving.cxdata.config.DynamicResourcePath;
import com.zving.cxdata.config.ExcludeResourcePath;
import com.zving.cxdata.config.StaticResourcePath;
import com.zving.framework.template.AbstractExecuteContext;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCContent;

public class CXURLModifier implements ICoreURLModifier {
  public static final String ID = "CXURLModifier";
  private static String staticResource = "images,public,special,css,js,resource,";
  private static String dynamicResource = "upload,";
  public String getExtendItemID() {
    return ID;
  }
  
  public String getExtendItemName() {
    return "车享URL修饰";
  }
  
  public boolean pass(ZCCatalog catalog) {
    return true;
  }
  
  public String modify(ZCCatalog catalog, String prefix, String platformID) {
    String urlPrefix = PublishPointBL.getCatalogURLPrefix(catalog);
    if (StringUtil.isEmpty(urlPrefix)) {
      urlPrefix = prefix;
    }
    return urlPrefix + CatalogUtil.getFullPath(catalog.getID());
  }
  
  public String modify(ZCContent content, String prefix, String platformID) {
    ZCCatalog catalog = CatalogUtil.getDAO(content.getCatalogID());
    String urlPrefix = PublishPointBL.getCatalogURLPrefix(catalog);
    if (StringUtil.isEmpty(urlPrefix)) {
      urlPrefix = prefix;
    }
    return urlPrefix + DetailNameRule.getContentPath(content);
  }
  
  public String modifyResource(AbstractExecuteContext context, String path){
	if (isDynamicResource(path)) {
		return ResourceURLBL.getCMSDynamicResourceURL(context, path);
	} else {
		return ResourceURLBL.getCMSStatictResourceURL(context, new Object[]{path});
	}
    
  }
  
  public boolean passResource(AbstractExecuteContext context, String path){
	  if (StringUtil.isEmpty(path)) {
		  return true;
	  }
	  if ( isDynamicResource(path) || isStaticResource(path)) {
		  String excludeResource = ExcludeResourcePath.getValue();
		  if (StringUtil.isNotEmpty(excludeResource)) {
			  for (String pathRegex : excludeResource.split(",")) {
				  if (path.matches(pathRegex)) {
					  return true;
				  }
			  }
		  }
		  return false;
	  } else {
		  return true;
	  }
  }
 public static void main(String[] args) {
	System.out.println("template/testblock.template.html".matches(".+template\\.html"));
 }
  private String getPathPre(String path) {
	  if(path.startsWith("/")) {
		  path = path.substring(1);
	  }
	  int index = path.indexOf("/");
	  if (index != -1) {
		  return path.substring(0, index);
	  } else {
		  return path;
	  }
  }
  
  private boolean isDynamicResource(String path) {
	  String pathPre = getPathPre(path);
	  String drc = DynamicResourcePath.getValue();
	  String dynamicAll = dynamicResource;
	  if (StringUtil.isNotEmpty(drc)) {
		  dynamicAll =dynamicAll + drc;
	  }
	  return ObjectUtil.in(pathPre, dynamicAll.split(","));
  }
  
 private boolean isStaticResource(String path) {
	 String pathPre = getPathPre(path);
	 String staticAll = staticResource;
	 String src = StaticResourcePath.getValue();
	  if (StringUtil.isNotEmpty(src)) {
		  staticAll = staticAll + src;
	  }
	  return ObjectUtil.in(pathPre, staticAll.split(","));
  }
 
  public void setPrefix(AbstractExecuteContext context) {
	  context.addDataVariable("Prefix", context.eval("Site.URL"));
  }
}
